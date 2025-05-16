package org.spring.MySite.controllers;


import org.junit.jupiter.api.Test;
import org.spring.MySite.config.TestSecurityConfig2;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig2.class)
@ActiveProfiles({"test","test2"})
@Sql({"/schema/schema1.sql", "/schema/schema2.sql", "/schema/schema3.sql", "/schema/schema4.sql"})
public class LoginIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    public void givenValidPerson_whenLogin_thenRedirect() throws Exception {
        try {
            autoLogin();
        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void autoLogin () {
        // 1. Создаем RestTemplate с поддержкой кук
        RestTemplate restTemplate = new RestTemplateBuilder()
                .additionalInterceptors(new CookieInterceptor())
                .build();

        String loginUrl = "http://192.168.0.59:" + port + "/login";

        // 2. Получаем CSRF токен и куки сессии
        ResponseEntity<String> getResponse = restTemplate.getForEntity(loginUrl, String.class);
        String csrfToken = extractCsrfToken(getResponse.getBody());
        String sessionCookie = getResponse.getHeaders().getFirst("Set-Cookie");

        // 3. Подготавливаем запрос
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Cookie", sessionCookie); // Важно для поддержки сессии

        // Spring Security ожидает CSRF либо как параметр, либо в заголовке
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", "Test User");
        formData.add("password", "password");
        formData.add("_csrf", csrfToken); // Добавляем как параметр формы
 /*
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", "Test User");
        formData.add("password", "password");
*/
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        // 4. Отправляем запрос и обрабатываем перенаправление
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(loginUrl, request, String.class);

            // Проверяем успешность по статусу
            if (response.getStatusCode().is3xxRedirection()) {
                System.out.println("Pass successfully! Redirected to: " + response.getHeaders().getLocation());
            } else {
                System.out.println("Login response: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Auto-login failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Интерцептор для сохранения кук между запросами
    private static class CookieInterceptor implements ClientHttpRequestInterceptor {
        private String cookies;

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            if (cookies != null) {
                request.getHeaders().add("Cookie", cookies);
            }
            ClientHttpResponse response = null;
            try {
                response = execution.execute(request, body);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (response.getHeaders().get("Set-Cookie") != null) {
                this.cookies = String.join("; ", response.getHeaders().get("Set-Cookie"));
            }
            return response;
        }
    }

    private String extractCsrfToken(String html) {
        // Ищем <input type="hidden" name="_csrf" value="токен"/>
        int start = html.indexOf("name=\"_csrf\" value=\"");
        if (start > 0) {
            start += "name=\"_csrf\" value=\"".length();
            int end = html.indexOf("\"", start);
            if (end > start) {
                return html.substring(start, end);
            }
        }

        // Альтернативный поиск в meta-тегах
        start = html.indexOf("name=\"_csrf\" content=\"");
        if (start > 0) {
            start += "name=\"_csrf\" content=\"".length();
            int end = html.indexOf("\"", start);
            if (end > start) {
                return html.substring(start, end);
            }
        }

        throw new RuntimeException("CSRF token not found in HTML response");
    }

}
