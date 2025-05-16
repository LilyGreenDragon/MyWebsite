package org.spring.MySite.controllers;

import org.spring.MySite.config.TestSecurityConfigWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfigWebTestClient.class)
@ActiveProfiles({"test", "testWebTestClient"})
public class RestPeopleControllerIntegrationTestWithWebTestClientWithMock {
/*
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PeopleService peopleService;

    @Test
    public void givenValidPerson_whenUpdatePerson_thenReturnIsOk() {
        Person validPerson = new Person("John", "Jonson", LocalDate.of(1990, 1, 1));

        doNothing().when(peopleService).save(any(Person.class));

        webTestClient.post()
                .uri("/REST/myPage")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validPerson)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("The user was updated successfully");

        verify(peopleService).save(any(Person.class));
    }

    @Test
    public void givenInvalidPerson_whenUpdatePerson_thenReturnIsBadRequest() {

        Person invalidPerson = new Person();
        invalidPerson.setName("John");
        invalidPerson.setSurname("Jonsonnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");  // invalidPerson.setSurname("J".repeat(51)); // 51 символ (>50)
        invalidPerson.setBirthdate(LocalDate.of(1000, 1, 1)); // invalidPerson.setBirthdate(LocalDate.now().plusDays(1)); // Дата в будущем

        webTestClient.post()
                .uri("/REST/myPage")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidPerson)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(response -> {
                    assertThat(response)
                            .contains("surname-")
                            .contains("birthdate-");
                });
        verify(peopleService, never()).save(any());
    }
*/

}
