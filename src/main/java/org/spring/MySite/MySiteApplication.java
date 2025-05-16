package org.spring.MySite;

import org.spring.MySite.DTO.RegisterDTO;
import org.spring.MySite.models.PasswordIn;
import org.spring.MySite.models.Person;
import org.spring.MySite.services.PeopleService;
import org.spring.MySite.services.RegistrationService;
import org.spring.MySite.services.RolesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.Optional;


@SpringBootApplication
public class MySiteApplication implements CommandLineRunner {

	private PeopleService peopleService;
	private RolesService rolesService;
	private PasswordEncoder passwordEncoder;
	private RegistrationService registrationService;

	@Autowired
	public MySiteApplication(PeopleService peopleService, RolesService rolesService, PasswordEncoder passwordEncoder, RegistrationService registrationService) {
		this.peopleService = peopleService;
		this.rolesService = rolesService;
		this.passwordEncoder = passwordEncoder;
		this.registrationService = registrationService;
	}

	public static void main(String[] args) {
		SpringApplication.run(MySiteApplication.class, args);
	}

	 @Override
	public void run(String... args) throws Exception {
/*
		Optional<Person> person = peopleService.findByUsername("Tony");
		System.out.println(person);
		if (person.isEmpty()) {
			PasswordIn password = new PasswordIn();
			RegisterDTO adminDTO = new RegisterDTO("Tony", "1111", "tony@mail.ru", password.getPasswordReg());
			registrationService.registerAdmin(adminDTO);
		}
*/
		//autoLogin();
	}

	private void autoLogin() {
		// 1. Создаем RestTemplate с поддержкой кук
		RestTemplate restTemplate = new RestTemplateBuilder()
				.additionalInterceptors(new CookieInterceptor())
				.build();

		String loginUrl = "http://192.168.0.59:8080/login";

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
		formData.add("username", "Tony");
		formData.add("password", "1111");
		formData.add("_csrf", csrfToken); // Добавляем как параметр формы

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

		// 4. Отправляем запрос и обрабатываем перенаправление
		try {
			ResponseEntity<String> response = restTemplate.postForEntity(loginUrl, request, String.class);

			// Проверяем успешность по статусу
			if (response.getStatusCode().is3xxRedirection()) {
				System.out.println("Login successful! Redirected to: " + response.getHeaders().getLocation());
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
