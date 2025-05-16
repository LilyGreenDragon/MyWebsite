package org.spring.MySite.controllers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.spring.MySite.config.TestSecurityConfig1;
import org.spring.MySite.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.HttpClientErrorException;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig1.class)
@ActiveProfiles({"test","test1"})
@Sql({"/schema/schema1.sql", "/schema/schema2.sql", "/schema/schema3.sql", "/schema/schema4.sql"})

public class RestPeopleControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

  //  @MockBean
  //  PersonDetailsService personDetailsService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private Validator validator;

    @Test //не тест, узнаем порт
    void printH2ConsoleUrl() {
        System.out.println("H2 Console URL: http://localhost:" + port + "/h2-console");
    }

    @Test //не тест, проверяем какая БД подключена
    void printDataSource() {
        try {
            System.out.println("Используемая БД: " + dataSource.getConnection().getMetaData().getURL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Test // не тест, просмотр таблицы
    void testDatabaseTablePerson() {

        jdbcTemplate.query("SELECT * FROM person", (rs, rowNum) -> {
            System.out.println(rs.getString("id") + "\t" + rs.getString("username") + "\t" + rs.getString("email"));
            return null;
        });
    }

    @Test //не тест, проверяем валидатор
    public void testValidatorDirectly() {
        Person invalid = new Person();
        invalid.setSurname("J".repeat(51));

        Set<ConstraintViolation<Person>> violations = validator.validate(invalid);
        assertFalse(violations.isEmpty());
        violations.forEach(v -> System.out.println(v.getMessage()));
    }

    @Test
    public void whenGetPerson_thenReturnPersonAndIsOk() throws Exception {
        // Создаем тестового пользователя с ролью
      /*  Role userRole = new Role();
        userRole.setId(2);
        userRole.setName("USER");
        Person testPerson = new Person(1, "Test User", "password", "test@mail.ru");
        testPerson.setRoles(List.of(userRole)); */

        // Мокируем UserDetailsService
      /*  when(personDetailsService.loadUserByUsername("Test User"))
                .thenReturn(new PersonDetails(testPerson)); */

        // Создание аутентификации
    /*   Authentication auth = new UsernamePasswordAuthenticationToken(
                new PersonDetails(testPerson), // principal
                null, // credentials
                Collections.singletonList(new SimpleGrantedAuthority("USER"))
        );

        // Установка SecurityContext
        SecurityContextHolder.getContext().setAuthentication(auth);

        //Проверяем контекст
        PersonDetails personDetails = (PersonDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Person person = personDetails.getPerson();
        System.out.println("Из контекста " +person);
        //или как ниже
        // Убедитесь, что Principal действительно содержит PersonDetails
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof PersonDetails) {
            PersonDetails details = (PersonDetails) authentication.getPrincipal();
            System.out.println("Person: " + details.getPerson());
        } else {
            System.out.println("Principal is: " + authentication.getPrincipal().getClass());
        } // это все не работало, при запросе restTemplate в контексте не оказывалось пользователя, тк запрос был в другом потоке.
        // решение: создание TestAuthFilter, который устанавливает пользователя в контекст.
*/

        ResponseEntity<Person> response = restTemplate.exchange(
                "http://192.168.0.59:" + port + "/REST/myPage",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                Person.class
        );

        // Отладочная информация
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@mail.ru", response.getBody().getEmail());
    }

    @Test
    public void givenValidPerson_whenUpdatePerson_thenReturnIsOk() throws Exception{

    /*
        jdbcTemplate.batchUpdate(
        "INSERT INTO roles (name) VALUES (?)",
        Arrays.asList(
        new Object[] {"ADMIN"},
        new Object[] {"USER"},
        new Object[] {"BLOCKED"}
        )
        );
        jdbcTemplate.update(
                "INSERT INTO person (id, username, password, email) VALUES (?, ?, ?, ?)",
                1, "Test User", "password", "test@mail.ru"
        );
        jdbcTemplate.update(
                "INSERT INTO person_roles (person_id,role_id) VALUES (?, ?)",
                1, 2
        );
*/
        String requestJson = """
            {
                "name": "Ivanka",
                "surname": "Ivanova",
                "birthdate": "1990-01-01"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        String url = "http://192.168.0.59:" + port + "/REST/myPage";

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );

      /*  if (response.getStatusCode().is3xxRedirection()) {
            System.out.println("Redirected to: " + response.getHeaders().getLocation());
        } */

        // Отладочная информация
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        jdbcTemplate.query("SELECT * FROM person", (rs, rowNum) -> {
            System.out.println(rs.getString("id") + "\t" + rs.getString("username") + "\t" + rs.getString("name") +
                    "\t" + rs.getString("surname"));
            return null;
        });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("updated successfully"));

    }

    @Test
    public void givenInvalidPerson_whenUpdatePerson_thenReturnIsBadRequest() throws Exception{

        Person invalidPerson = new Person();
        invalidPerson.setName("John");
        invalidPerson.setSurname("Jonsonnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");  // invalidPerson.setSurname("J".repeat(51)); // 51 символ (>50)
        invalidPerson.setBirthdate(LocalDate.of(1000, 1, 1)); // invalidPerson.setBirthdate(LocalDate.now().plusDays(1)); // Дата в будущем

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = "http://192.168.0.59:" + port + "/REST/myPage";

            // Отправка запроса
      try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(invalidPerson, headers),
                    String.class
            );

        // Отладочная информация
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("surname-"));
        assertTrue(response.getBody().contains("birthdate-"));

      } catch (HttpClientErrorException e) {
            // Этот блок будет ловить только неожиданные ошибки, можно без него
            fail("Получено неожиданное исключение: " + e.getMessage());
      }


    }


}
