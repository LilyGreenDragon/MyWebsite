package org.spring.MySite.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spring.MySite.MySiteApplication;
import org.spring.MySite.config.TestAuthFilter;
import org.spring.MySite.config.TestSecurityConfig1;
import org.spring.MySite.models.Person;
import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.PeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = {
                MySiteApplication.class,
                TestSecurityConfig1.class // Дополнительные конфиги
        }
)

@AutoConfigureMockMvc(addFilters = true) // Важно: включает все встроенные фильтры, доп фильтры надо подключать отдельно в @BeforeEach

@ActiveProfiles({"test","test1"})
public class RestPeopleControllerIntegrationTestWithMock {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private PeopleService peopleService;

    @Autowired
    private TestAuthFilter testAuthFilter;

    @Autowired
    WebApplicationContext webApplicationContext;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(testAuthFilter, "/REST/*") // Фильтр применяется к URL
                .build();
    }

       @Test
       public void givenValidPerson_whenUpdatePerson_thenReturnIsOk() throws Exception {

        Person validPerson = new Person("John","Jonson",LocalDate.of(1990, 1, 1));

        doNothing().when(peopleService).save(any(Person.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/REST/myPage")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPerson)))
                .andDo(result -> {
                    System.out.println("SecurityContext: " +
                            SecurityContextHolder.getContext().getAuthentication());
                })
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("The user was updated successfully"));

        verify(peopleService).save(any(Person.class));

    }

    }

