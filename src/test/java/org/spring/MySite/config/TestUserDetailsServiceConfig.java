package org.spring.MySite.config;

import org.spring.MySite.models.Person;
import org.spring.MySite.models.Role;
import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.PersonDetailsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@TestConfiguration
public class TestUserDetailsServiceConfig {

    @Bean
    @Primary
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new PersonDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                if ("Tony".equals(username)) {
                    Person person = new Person(
                            1,
                            "Tony",
                            "1111", //encoder.encode("1111")
                            "tony@mail.ru"
                    );
                    person.setRoles(Collections.singletonList(new Role("ADMIN")));
                    return new PersonDetails(person);
                }
                throw new UsernameNotFoundException("User not found");
            }
        };
    }
}
