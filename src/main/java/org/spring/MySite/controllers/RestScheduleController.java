package org.spring.MySite.controllers;

import org.spring.MySite.models.Lesson;
import org.spring.MySite.models.Person;
import org.spring.MySite.repositories.LessonsRepository;

import org.spring.MySite.security.P;
import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.PeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
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
    private LessonsRepository lessonsRepository;

    @Autowired
    private PeopleService peopleService;

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @GetMapping("/all")
    public ResponseEntity<List<Lesson>> getAllLessons(@P Person updatedPerson) {
        System.out.println("Из /all "+ updatedPerson);
        List<Lesson> lessons = lessonsRepository.findAllByOrderByDayOfWeekAscStartTimeAsc();
        return ResponseEntity.ok(lessons);
    }

    @PostMapping("/mylessons/{lessonId}")
    public ResponseEntity<?> addLessonToMySchedule(@PathVariable Long lessonId, @P Person updatedPerson, Authentication authentication) {
        System.out.println(updatedPerson);
        System.out.println("ID урока " + lessonId);

        try {
            System.out.println("В добавлении урока методе");
            // Находим урок
            Lesson lesson = lessonsRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Урок не найден: " + lessonId));

            // Проверяем, есть ли уже такой урок у пользователя
            if (updatedPerson.getLessons().contains(lesson)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Урок уже добавлен в расписание"));
            }

            // Добавляем урок
            updatedPerson.getLessons().add(lesson);
            peopleService.save(updatedPerson);
            updateAllUserSessions(authentication);

            return ResponseEntity.ok(Map.of(
                    "message", "Урок успешно добавлен",
                    "lessonId", lessonId,
                    "personId", updatedPerson.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ошибка при добавлении урока: " + e.getMessage()));
        }
    }


    @DeleteMapping("/mylessons/{lessonId}")
    public ResponseEntity<?> removeLessonFromMySchedule(@PathVariable Long lessonId, @P Person updatedPerson, Authentication authentication) {
        try {
            System.out.println("В delete методе");
            boolean removed = updatedPerson.getLessons().removeIf(lesson -> lesson.getId().equals(lessonId));
            System.out.println("Удаление "+ removed);
            if (removed) {
                System.out.println("Удаление "+ updatedPerson);
                peopleService.save(updatedPerson);
                System.out.println("Урок успешно удален. Уроков ПОСЛЕ удаления: " + updatedPerson.getLessons().size());
                updateAllUserSessions(authentication);

                return ResponseEntity.ok(Map.of(
                        "message", "Урок успешно удален",
                        "lessonId", lessonId,
                        "success", true
                ));
            } else {
                System.out.println("Урок с ID " + lessonId + " не найден в расписании пользователя");

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "message", "Урок не найден в расписании пользователя",
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

    @GetMapping("/allmylessons")
    public ResponseEntity<List<Lesson>> getMyLessons(@P Person updatedPerson) {
        System.out.println("В allmylessons методе");
        List<Lesson> lessons = updatedPerson.getLessons();
        return ResponseEntity.ok(lessons);
    }

    //Метод нужен если сессии хранятся в redis,работает когда у пользователя несколько сессий
    public void updateAllUserSessions(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof PersonDetails personDetails) {
            String username = personDetails.getUsername();

            // Приводим тип sessionRepository
            FindByIndexNameSessionRepository<Session> castedRepo =
                    (FindByIndexNameSessionRepository<Session>) sessionRepository;

            Map<String, Session> sessions = castedRepo.findByPrincipalName(username);

            for (Map.Entry<String, Session> entry : sessions.entrySet()) {
                Session session = entry.getValue();

                SecurityContext newContext = new SecurityContextImpl();
                newContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                        personDetails,
                        personDetails.getPassword(),
                        personDetails.getAuthorities()
                ));

                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, newContext); //сейчас SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT"
                castedRepo.save(session);
            }
        }
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
    public ResponseEntity<?> deleteLesson(@PathVariable Long id) {
        try {
            Lesson lesson = lessonsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Урок не найден с id: " + id));

            lesson.getPeople().clear();  // очищаем список людей, связанных с уроком

            lessonsRepository.delete(lesson);
            return ResponseEntity.ok(Map.of(
                    "message", "Урок успешно удален",
                    "lessonId", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ошибка при удалении урока: " + e.getMessage()));
        }
    }

}
