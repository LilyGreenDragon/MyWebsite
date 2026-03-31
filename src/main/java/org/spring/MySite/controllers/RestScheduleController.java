package org.spring.MySite.controllers;

import jakarta.persistence.EntityManager;
import org.spring.MySite.models.Lesson;
import org.spring.MySite.models.Person;
import org.spring.MySite.repositories.LessonsRepository;

import org.spring.MySite.security.P;
import org.spring.MySite.security.PDB;
import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.LessonsService;
import org.spring.MySite.services.PeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/REST/schedule")
public class RestScheduleController {

    @Autowired
    private LessonsService lessonsService;

    @Autowired
    private LessonsRepository lessonsRepository;

    @Autowired
    private PeopleService peopleService;

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/all")
    public ResponseEntity<List<Lesson>> getAllLessons() {
        List<Lesson> lessons = lessonsRepository.findAllByOrderByDayOfWeekAscStartTimeAsc();
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/allmylessons")
    public ResponseEntity<List<Lesson>> getMyLessons(@P Person updatedPerson) {
        List<Lesson> lessons = updatedPerson.getLessons();
        System.out.println("ALL MY LESSONS " + updatedPerson);
        return ResponseEntity.ok(lessons);
    }

    @PostMapping("/mylessons/{lessonId}")
    public ResponseEntity<?> addLessonToMySchedule(@PathVariable Long lessonId, @PDB Person updatedPersonDB, Authentication authentication) {

        try {

            System.out.println("В методе добавления урока");

            Lesson lesson = lessonsRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Урок не найден: " + lessonId));

            // Проверяем, есть ли уже такой урок у пользователя
            if (updatedPersonDB.getLessons().stream()
                    .anyMatch(l -> l.getId().equals(lessonId))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Урок уже добавлен в расписание"));
            }

            updatedPersonDB.getLessons().add(lesson);
            peopleService.save(updatedPersonDB);
            updateAllUserSessions(updatedPersonDB);

            return ResponseEntity.ok(Map.of(
                    "message", "Урок успешно добавлен",
                    "lessonId", lessonId,
                    "personId", updatedPersonDB.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ошибка при добавлении урока: " + e.getMessage()));
        }
    }


    @DeleteMapping("/mylessons/{lessonId}")
    public ResponseEntity<?> removeLessonFromMySchedule(@PathVariable Long lessonId, @PDB Person updatedPersonDB) {
        try {
            System.out.println("В delete методе");
            boolean removed = updatedPersonDB.getLessons().removeIf(lesson -> lesson.getId().equals(lessonId));
            System.out.println("Есть ли урок для удаления "+ removed);
            if (removed) {
                System.out.println("Человек у которого удалился урок(уже без урока)"+ updatedPersonDB);
                peopleService.save(updatedPersonDB);
                System.out.println("Урок успешно удален. Уроков ПОСЛЕ удаления: " + updatedPersonDB.getLessons().size());

                updateAllUserSessions(updatedPersonDB);

                return ResponseEntity.ok(Map.of(
                        "message", "Урок успешно удален",
                        "lessonId", lessonId,
                        "success", true
                ));
            } else {
                System.out.println("Урок с ID " + lessonId + " не найден в расписании пользователя");

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "code", "LESSON_NOT_FOUND",
                                "message", "Урок не найден в расписании пользователя.",
                                "lessonId", lessonId,
                                "success", false
                        ));
            }

        } catch (Exception e) {
            System.err.println("Ошибка при удалении урока: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Ошибка при удалении урока: " + e.getMessage(),
                            "success", false
                    ));
        }
    }


    //Метод нужен если сессии хранятся в redis,работает когда у пользователя несколько сессий
    public void updateAllUserSessions(Person updatedPersonDB) {
        String username = updatedPersonDB.getUsername();

        PersonDetails freshDetails = new PersonDetails(updatedPersonDB);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                freshDetails,
                freshDetails.getPassword(),
                freshDetails.getAuthorities()
        );

        // Обновляем текущий SecurityContext
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // Обновляем все сессии в Redis
        FindByIndexNameSessionRepository<Session> castedRepo =
                (FindByIndexNameSessionRepository<Session>) sessionRepository;

        Map<String, Session> sessions = castedRepo.findByPrincipalName(username);

        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            Session session = entry.getValue();

            SecurityContext newContext = new SecurityContextImpl();
            newContext.setAuthentication(newAuth);

            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    newContext
            );
            castedRepo.save(session);
        }

        System.out.println("✅ Обновлены сессии для пользователя: " + username);
    }


    @PostMapping("/lessons")
    public ResponseEntity<?> createLesson(@RequestBody Lesson lesson,  BindingResult bindingResult) {
        System.out.println("В методе createLesson");
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                System.out.println(errors);
            }
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

            Lesson savedLesson = lessonsRepository.save(lesson);
        System.out.println(savedLesson);

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of(
                    "message", "Урок успешно создан",
                    "lesson", savedLesson
            ));
    }

    @PutMapping("/lessons/{id}")
    public ResponseEntity<?> updateLesson(@PathVariable Long id, @RequestBody Lesson lessonData, BindingResult bindingResult) {

        Lesson lesson = lessonsService.findById(id);
        if (lesson == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "code", "LESSON_NOT_FOUND",
                            "message", "Урок не найден"
                    ));
        }

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

            Lesson existingLesson = lessonsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Урок не найден с id: " + id));


        // Обновляем поля
        existingLesson.setLessonName(lessonData.getLessonName());
        existingLesson.setTeacherName(lessonData.getTeacherName());
        existingLesson.setRoomName(lessonData.getRoomName());
        existingLesson.setDayOfWeek(lessonData.getDayOfWeek());
        existingLesson.setStartTime(lessonData.getStartTime());
        existingLesson.setEndTime(lessonData.getEndTime());

            Lesson updatedLesson = lessonsRepository.save(existingLesson);

            return ResponseEntity.ok(Map.of(
                    "message", "Урок успешно обновлен",
                    "lesson", updatedLesson
            ));

    }

    @DeleteMapping("/lessons/{id}")
    @Transactional
    public ResponseEntity<?> deleteLesson(@PathVariable Long id, @P Person updatedPerson) {
        try {
            System.out.println("=== УДАЛЕНИЕ УРОКА ID: " + id + " ===");

            Lesson lesson = lessonsService.findById(id);
            if (lesson == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "code", "LESSON_NOT_FOUND",
                                "message", "Урок не найден"
                        ));
            }

            // НАХОДИМ ВСЕХ ПОЛЬЗОВАТЕЛЕЙ С ЭТИМ УРОКОМ
            List<Person> usersWithLesson = peopleService.findPersonsByLessonId(id);

            // Сначала удаляем урок из коллекций пользователей (в памяти). Удаляет урок из Hibernate кэша на сервере приложения.
            for (Person person : usersWithLesson) {
                boolean removed = person.getLessons().removeIf(l -> l.getId().equals(id));
                System.out.println("У пользователя " + person.getUsername() + " урок удален? " + removed);
            }

            // Удаляем урок
            lessonsRepository.delete(lesson);
            System.out.println("DELETE LESSON " + updatedPerson);


/*
            // Принудительно сбрасываем кэш
            entityManager.flush();  // Принудительно выполняет все SQL,влияет ТОЛЬКО на текущую сессию
            entityManager.clear(); // Очищает ВЕСЬ персистентный контекст,влияет ТОЛЬКО на текущую сессию
*/
            // Проверяем, удалился ли урок
            boolean exists = lessonsRepository.existsById(id);
            System.out.println("❓ Урок всё еще в БД? " + exists);

            // Обновляем сессии пользователей
            for (Person person : usersWithLesson) {
                updateUserSessionsByUsername(person.getUsername());
            }


            //Обновляем контекст текущего пользователя
            boolean isCurrentUser = usersWithLesson.stream()
                    .anyMatch(p -> p.getUsername().equals(updatedPerson.getUsername()));

            if (isCurrentUser) {
                Person freshPerson = peopleService.findByUsername(updatedPerson.getUsername()).orElseThrow();
                PersonDetails freshDetails = new PersonDetails(freshPerson);
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        freshDetails,
                        freshDetails.getPassword(), //auth.getCredentials(),
                        freshDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                System.out.println("✅ Текущий контекст обновлен");
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Урок успешно удален",
                    "lessonId", id,
                    "deleted", !exists
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ошибка: " + e.getMessage()));
        }
    }

    public void updateUserSessionsByUsername(String username) {
        try {
            // Приводим тип sessionRepository
            FindByIndexNameSessionRepository<Session> castedRepo =
                    (FindByIndexNameSessionRepository<Session>) sessionRepository;

            // Находим все сессии пользователя
            Map<String, Session> sessions = castedRepo.findByPrincipalName(username);

            // Получаем актуального пользователя из БД
            Person freshPerson = peopleService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));

            // Создаем PersonDetails с актуальными данными
            PersonDetails personDetails = new PersonDetails(freshPerson);

            // Обновляем каждую сессию
            for (Map.Entry<String, Session> entry : sessions.entrySet()) {
                Session session = entry.getValue();

                SecurityContext newContext = new SecurityContextImpl();
                newContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                        personDetails,
                        personDetails.getPassword(),
                        personDetails.getAuthorities()
                ));

                session.setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        newContext
                );
                castedRepo.save(session);
            }

            System.out.println("Обновлены сессии для пользователя: " + username + " (сессий: " + sessions.size() + ")");
            System.out.println("Пользователь "+ personDetails);
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении сессий пользователя " + username + ": " + e.getMessage());
        }
    }

}
