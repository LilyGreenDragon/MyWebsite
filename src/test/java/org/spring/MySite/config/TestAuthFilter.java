package org.spring.MySite.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.spring.MySite.models.Person;
import org.spring.MySite.models.Role;
import org.spring.MySite.security.PersonDetails;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Profile("test")
public class TestAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Создаем тестового пользователя
        Person testPerson = new Person(1, "Test User", "password", "test@mail.ru");
        Role userRole = new Role();
        userRole.setId(2);
        userRole.setName("USER");
        testPerson.setRoles(List.of(userRole));
        PersonDetails userDetails = new PersonDetails(testPerson);

        // Устанавливаем аутентификацию
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}