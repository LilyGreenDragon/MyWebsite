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

		Optional<Person> person = peopleService.findByUsername("Tony");
		System.out.println(person);
		if (person.isEmpty()) {
			PasswordIn password = new PasswordIn();
			RegisterDTO adminDTO = new RegisterDTO("Tony", "1111", "tony@mail.ru", password.getPasswordReg());
			registrationService.registerAdmin(adminDTO);
		}
	}
}
