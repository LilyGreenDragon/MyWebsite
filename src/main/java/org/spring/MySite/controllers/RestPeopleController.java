package org.spring.MySite.controllers;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import org.spring.MySite.models.Person;
import org.spring.MySite.repositories.PeopleRepository;
import org.spring.MySite.security.P;
import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.PeopleService;
import org.spring.MySite.util.PersonErrorResponse;
import org.spring.MySite.util.PersonNotCreatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/REST")
public class RestPeopleController {

    private PeopleService peopleService;
    private JavaMailSender mailSender;
    private SessionRegistry sessionRegistry;

    @Autowired
    public RestPeopleController(PeopleService peopleService, JavaMailSender mailSender, SessionRegistry sessionRegistry) {
        this.peopleService = peopleService;
        this.mailSender = mailSender;
        this.sessionRegistry = sessionRegistry;
    }


  /*  @GetMapping("/myPage")
    public ResponseEntity<?> getPerson(){
        PersonDetails personDetails = (PersonDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Person person = personDetails.getPerson();
        System.out.println(person);
        return new ResponseEntity<>(person, HttpStatus.OK);
    }
*/

    @GetMapping("/session-info")
    public String sessionInfo(HttpSession session) {
        System.out.println("Session timeout: " + session.getMaxInactiveInterval() + " sec");
        return "Session timeout: " + session.getMaxInactiveInterval() + " sec";
    }

    @GetMapping("/myPage")
    public ResponseEntity<?> getPerson(@AuthenticationPrincipal PersonDetails personDetails){
        return new ResponseEntity<>(personDetails.getPerson(), HttpStatus.OK);
    }

   /* public Map<String, Object> get() {
        PersonDetails personDetails = (PersonDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Person person = personDetails.getPerson();
        System.out.println("go ok");
        System.out.println(person);
        Map<String, Object> map = new HashMap<>();
        map.put("name", person.getName());
        map.put("surname", person.getSurname());
        map.put("birthdate", person.getBirthdate());
        return map;
    }*/

    /*public ResponseEntity<HttpStatus> param (@RequestBody Map<String,String> param, @P Person updatedPerson) {
        String x =param.get("x");
        String y =param.get("y");
        return ResponseEntity.ok(HttpStatus.OK);
    }*/

    /*@GetMapping("/paramAjax")
    public Map<String, Object> getPersonAjax(){
        Map<String, Object> map = new HashMap<>();
        map.put("p", "200");
        map.put("y", "222");
        return map;
    }*/


    @PostMapping("/param")
    public ResponseEntity<HttpStatus> param (@RequestBody @Valid Person person, BindingResult bindingResult, @RequestParam("p") String p, @RequestParam("y") String y, @P Person updatedPerson) {
        if (bindingResult.hasErrors()) {

            List<String> err = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            System.out.println(err);

            StringBuilder errorMessage = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for(FieldError error: errors)
                errorMessage.append(error.getField()).append("-").append(error.getDefaultMessage()).append(";");

            throw new PersonNotCreatedException(errorMessage.toString());
        }
        System.out.println(person.getName());
        System.out.println(p);
        System.out.println(y);
        return ResponseEntity.ok(HttpStatus.OK);
    }


    @PostMapping("/myPage")
    public ResponseEntity<?> updatePerson(@RequestBody @Valid Person person, BindingResult bindingResult, @P Person updatedPerson) {

      if (bindingResult.hasErrors()) {
            //System.out.println("bindingResultUpdatePerson");
            List<String> err = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            System.out.println(err);

            StringBuilder errorMessage = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for(FieldError error: errors)
                errorMessage.append(error.getField()).append("-").append(error.getDefaultMessage()).append(";");

            throw new PersonNotCreatedException(errorMessage.toString());
        }
        System.out.println(person);
        updatedPerson.setName(person.getName());
        updatedPerson.setSurname(person.getSurname());
        updatedPerson.setBirthdate(person.getBirthdate());
        peopleService.save(updatedPerson);

        //return ResponseEntity.ok("{\"success\": \"ok\"}");
        return new ResponseEntity<>("The user was updated successfully",HttpStatus.OK);
    }

    @PostMapping("/myPage/mail")
    public ResponseEntity<?> eMail(@RequestBody @Valid Person person, BindingResult bindingResult, @P Person personMail) {
        if(bindingResult.hasErrors()) {
            //System.out.println("bindingResultEMail");
            List<String> err = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            System.out.println(err);

            StringBuilder errorMessage = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for(FieldError error: errors)
                errorMessage.append(error.getField()).append("-").append(error.getDefaultMessage()).append(";");

            throw new PersonNotCreatedException(errorMessage.toString());}

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String htmlMsg = person.getMessage();
//mimeMessage.setContent(htmlMsg, "text/html"); /** Use this or below line **/
        try {
            helper.setText(htmlMsg, true); // Use this or above line.
            helper.setTo("egorchik_mail@mail.ru"); //Site14789
            helper.setSubject("Сообщение от " + personMail.getUsername()+ " "+ personMail.getEmail());
            helper.setFrom("egorchik_mail@mail.ru");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        mailSender.send(mimeMessage);

        return new ResponseEntity<>("The email has been sent",HttpStatus.OK);
    }

    @PostMapping("/myPage/photo/delete")
    public ResponseEntity<?> deletePhoto(@RequestBody Person person, @P Person updatedPerson) {
        updatedPerson.setPhoto(person.getPhoto());
        peopleService.save(updatedPerson);
        return new ResponseEntity<>("The user was deleted successfully",HttpStatus.OK);
    }


    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handleException(PersonNotCreatedException e) {
        PersonErrorResponse response = new PersonErrorResponse(e.getMessage(), System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

    }



}
