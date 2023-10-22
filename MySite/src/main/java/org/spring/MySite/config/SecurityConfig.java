package org.spring.MySite.config;

import org.spring.MySite.security.*;

import org.spring.MySite.services.PersonDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.context.request.RequestContextListener;

import static jakarta.servlet.DispatcherType.ERROR;

@ComponentScan(basePackages = { "org.spring.MySite" })
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private PersonDetailsService personDetailsService;
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    private CustomAuthenticationFailureHandler authenticationFailureHandler;
    private CustomAccessDeniedHandler accessDeniedHandler;

@Autowired
    public SecurityConfig(PersonDetailsService personDetailsService, CustomAuthenticationSuccessHandler authenticationSuccessHandler, CustomAuthenticationFailureHandler authenticationFailureHandler, CustomAccessDeniedHandler accessDeniedHandler) {
        this.personDetailsService = personDetailsService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
    this.accessDeniedHandler = accessDeniedHandler;
}

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //.csrf().disable()
                /*.addFilterBefore(new RequestLoggerFilter(),
                BasicAuthenticationFilter.class)*/
                .authorizeHttpRequests((requests) -> requests

                        .requestMatchers("/", "/error","/js/**", "/css/**","/images/**", "/login", "/register","/userIsAbsent","/access-denied").permitAll()
                        .requestMatchers(HttpMethod.POST, "/register", "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/home","/myPage","/myPage/photo", "/myPage/mail", "/orders", "/myPage/photo/delete").hasAnyAuthority( "USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/pass","/block","/unblock").hasAuthority( "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/").hasAnyAuthority( "USER", "ADMIN")
                        .requestMatchers("/home", "/myPage", "/photo", "/news", "/holiday","/make", "/orders/current","/orderOk","/adminIn","/myPage/photo","/dr2021","/video").hasAnyAuthority( "USER", "ADMIN")
                        .requestMatchers("/admin","/show","/user/*","/loggedUsers").hasAuthority("ADMIN")

                        .dispatcherTypeMatchers(ERROR).permitAll()

                        .anyRequest().denyAll()
                )

                //.httpBasic(withDefaults())
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        //.defaultSuccessUrl("/", true)
                        //При желании вы можете принудительно заставить пользователя перейти на страницу design после входа в систему, даже если он находился на другой странце до входа в систему, передав значение true в качестве второго параметра в defaultSuccessUrl
                        //.failureUrl("/userIsAbsent")
                        //.failureUrl("/login?error")
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                        .permitAll()
                )
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .accessDeniedHandler(accessDeniedHandler)

                )

                .logout((logout) -> logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                        )

                .sessionManagement(config ->
                        config
                                //.sessionFixation().none()       // <---- Here
                               //.invalidSessionUrl("/invalidSession.html")
                                .maximumSessions(1)
                                .sessionRegistry(sessionRegistry())
                )

        .build();
    }


    /*@Bean
    public UserDetailsService userDetailsService() {
        UserDetails user =
                User.withDefaultPasswordEncoder()
                        .username("user")
                        .password("password")
                        .roles("user")
                        .build();

        UserDetails admin =
                User.withDefaultPasswordEncoder()
                        .username("admin")
                        .password("password")
                        .roles("admin")
                        .build();

        return new InMemoryUserDetailsManager(user,admin);
    }*/

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
    return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
}
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

//для sessionRegistry
    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
    }

//чтобы иметь возможность получить доступ к запросу из UserDetailsService
    @Bean public RequestContextListener requestContextListener(){
        return new RequestContextListener();
    }

//Прослушиватель неудачных попыток входа в систему
    @Bean
    public ApplicationListener loginFailureListener(){
        return new AuthenticationFailureEventListener();
    }
//Прослушиватель удачных попыток входа в систему
    @Bean
    public ApplicationListener loginSuccessListener(){
        return new AuthenticationSuccessEventListener();
    }


   /* @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(encoder());
    }*/

}
