package org.spring.MySite.config;

import org.spring.MySite.models.Person;
import org.spring.MySite.models.Role;
import org.spring.MySite.security.PersonDetails;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.List;
/*
@Profile("test")
public class TestAuthWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Создаем тестового пользователя
        Person testPerson = new Person(1, "Test User", "password", "test@mail.ru");
        Role userRole = new Role();
        userRole.setId(2);
        userRole.setName("USER");
        testPerson.setRoles(List.of(userRole));
        PersonDetails userDetails = new PersonDetails(testPerson);

        // Создаем аутентификацию
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("USER"))
        );

        // Создаем контекст безопасности
        SecurityContext context = new SecurityContext() {
            @Override
            public Authentication getAuthentication() {
                return auth;
            }

            @Override
            public void setAuthentication(Authentication authentication) {
                // Не требуется для тестов
            }
        };

        // Устанавливаем контекст и продолжаем цепочку фильтров
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
    }
}

 */