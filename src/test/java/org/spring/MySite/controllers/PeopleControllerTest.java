package org.spring.MySite.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spring.MySite.models.Person;
import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.PeopleService;
import org.spring.MySite.util.PersonNotCreatedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PeopleControllerTest {

    @Mock
    private PeopleService peopleService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private SessionRegistry sessionRegistry;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private PeopleController peopleController;

    private Person validPerson;
    private Person invalidPerson;
    private Person updatedPerson;

    @BeforeEach
    void setup() {
        updatedPerson = new Person(1, "Test User", "password", "test@mail.ru");
        validPerson = new Person("Robert", "Robertson", LocalDate.of(1990, 1, 1));
        invalidPerson = new Person("Robert", "Robertsonnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn", LocalDate.of(1700, 1, 1));
    }

    @Test
    void givenValidPerson_whenUpdatePerson_thenSave() {

        when(bindingResult.hasErrors()).thenReturn(false);
        String viewName = peopleController.update(validPerson, bindingResult, updatedPerson, authentication);

        assertEquals("redirect:/myPage", viewName);
        assertEquals(validPerson.getName(), updatedPerson.getName());
        assertEquals(validPerson.getSurname(), updatedPerson.getSurname());
        assertEquals(validPerson.getBirthdate(), updatedPerson.getBirthdate());
        verify(peopleService, times(1)).save(updatedPerson);
    }

    @Test
    void givenInvalidPerson_whenUpdatePerson_thenError()  {

        when(bindingResult.hasErrors()).thenReturn(true);

        FieldError fieldError1 = new FieldError("person", "surname", "Surname should be less then 50 characters");
        FieldError fieldError2 = new FieldError("person", "birthdate", "Date must not be before 1920-01-01");

        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        String viewName = peopleController.update(invalidPerson, bindingResult, updatedPerson, authentication);

        assertEquals("indexMyPage", viewName);
        verify(peopleService, never()).save(any());

    }

    @Test
    void givenPersonWithActiveSessions_whenDeletePerson_thenSessionExpire() {

        PersonDetails personDetails = new PersonDetails(updatedPerson);
        SessionInformation session = mock(SessionInformation.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Principal principal = mock(Principal.class);

        // Настройка моков:
        // Есть 1 активная сессия для пользователя
        when(sessionRegistry.getAllSessions(personDetails, false)).thenReturn(List.of(session));

        String result = peopleController.deletePerson(updatedPerson, authentication, request, response);

        // Проверки:
        verify(session).expireNow(); // Сессия должна быть завершена
        verify(sessionRegistry).removeSessionInformation(session.getSessionId()); // Информация о сессии удалена
        verify(peopleService).deleteById(updatedPerson.getId());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("redirect:/", result);
    }


}