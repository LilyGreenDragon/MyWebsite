package org.spring.MySite.controllers;

import org.junit.jupiter.api.Test;
import org.spring.MySite.config.TestSecurityConfigAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
        import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestSecurityConfigAdmin.class)
@ActiveProfiles("testAdmin")
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Test
    void testDatabaseConnection() throws SQLException {
        System.out.println("Используемая БД: " + dataSource.getConnection().getMetaData().getDatabaseProductName());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})  // Имитация аутентифицированного пользователя с ролью ADMIN
    @Sql({"/schema/schema1.sql", "/schema/schema2.sql", "/schema/schema3.sql", "/schema/schema4.sql"})
    public void givenPersonADMIN_whenGETRequest_thenReturnIsOk() throws Exception {
       mockMvc.perform(get("/admin/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("adminPage"))  // Проверяем имя возвращаемого шаблона
                .andExpect(model().attribute("people", hasSize(2))); // Ожидаем 2 записи из H2
               //.andExpect(model().attributeExists("passwordIn", "people", "peopleLogged", "rolesMap")); //без подключенной БД проверяет только наличие атрибутов в модели, а не их содержимое(они пустые)
    }

    @Test
    @WithAnonymousUser
    public void givenPersonUnauthenticated_whenGETRequest_thenReturnIs3xxRedirection() throws Exception {
        mockMvc.perform(get("/admin/admin"))
                .andExpect(status().is3xxRedirection())  // Ожидаем редирект на страницу логина
                .andExpect(redirectedUrlPattern("**/login"));  // Проверяем URL редиректа http://localhost/login
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"USER"})
    public void givenPersonUSER_whenGETRequest_thenReturnIsForbidden() throws Exception {
        mockMvc.perform(get("/admin/admin"))
                .andExpect(status().is3xxRedirection())  // Ожидаем редирект
                .andExpect(redirectedUrl("/access-denied"));  // Проверяем URL редиректа /access-denied
    }


}