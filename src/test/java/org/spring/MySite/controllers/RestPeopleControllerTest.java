package org.spring.MySite.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spring.MySite.models.Person;
import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.PeopleService;
import org.spring.MySite.services.RegistrationService;
import org.spring.MySite.services.RolesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@WebMvcTest(RestPeopleController.class)    // Изолированный unit-тест для MVC-слоя
@AutoConfigureMockMvc(addFilters = false)  // Отключает фильтры (например, Security)
public class RestPeopleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PeopleService peopleService;

    @MockBean
    private BindingResult bindingResult;

    @MockBean
    private RolesService rolesService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private JavaMailSender javaMailSender;

    @MockBean
    private SessionRegistry sessionRegistry;

    @MockBean
    private SecurityContext securityContext;

    @MockBean
    private Authentication authentication;

    @MockBean
    private PersonDetails personDetails;

    String urlRequest = "http://192.168.0.59:8080";


    @BeforeEach
    void setup() {
        Person testPerson = new Person(1, "Test User", "password", "test@mail.ru");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(personDetails);
        when(personDetails.getPerson()).thenReturn(testPerson);
        SecurityContextHolder.setContext(securityContext);
        //PersonDetails personDetails = new PersonDetails(updatedPerson);
    }

    @Test
    void givenValidPerson_whenUpdatePerson_thenReturnIsOk() throws Exception {

        Person validPerson = new Person("Robert", "Robertson", LocalDate.of(1990, 1, 1));

        // Выполнение запроса и проверки
        mockMvc.perform(post("/REST/myPage")
                        .with(csrf()) // Добавляем CSRF-токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPerson)))
                .andExpect(status().isOk())
                .andExpect(content().string("The user was updated successfully"));
    }

    @Test
    void givenInvalidPerson_whenUpdatePerson_thenReturnIsBadRequest() throws Exception {

        Person invalidPerson = new Person("Robert", "Robertsonnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn", LocalDate.of(1700, 1, 1));

        // Выполнение запроса и проверки
        mockMvc.perform(post("/REST/myPage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPerson)))
                .andExpect(status().isBadRequest()) //статус 400
                .andExpect(jsonPath("$.message").value(
                        containsString("surname-Surname should be less then 50 characters")))
                .andExpect(jsonPath("$.message").value(
                        containsString("birthdate-Date must not be before 1920-01-01")))
                .andExpect(jsonPath("$.timestamp").exists());

        // Проверка, что сервис не вызывался
        verify(peopleService, never()).save(any());
    }

}