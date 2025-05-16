package org.spring.MySite.config;

import org.modelmapper.ModelMapper;
import org.spring.MySite.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.context.request.RequestContextListener;

import static jakarta.servlet.DispatcherType.ERROR;


@Configuration
@EnableWebSecurity
@Profile("test2")
public class TestSecurityConfig2 {

    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    private CustomAuthenticationFailureHandler authenticationFailureHandler;
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    public TestSecurityConfig2(CustomAuthenticationSuccessHandler authenticationSuccessHandler, CustomAuthenticationFailureHandler authenticationFailureHandler, CustomAccessDeniedHandler accessDeniedHandler) {

        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //.csrf(csrf -> csrf.disable())
                //.addFilterBefore(new UsernamePasswordAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((requests) -> requests

                        .requestMatchers(HttpMethod.GET,"/", "/error","/js/**", "/css/**","/images/**","/imagecab/**", "/login", "/register","/userIsAbsent","/access-denied").permitAll()
                        .requestMatchers(HttpMethod.POST, "/register", "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/home","/myPage","/myPage/photo", "/myPage/mail", "/orders", "/myPage/photo/delete", "/REST/**").hasAnyAuthority( "USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/home", "/myPage", "/photo", "/news", "/holiday","/make", "/orders/current","/orderOk","/show","/admin/adminIn","/myPage/photo","/dr2021","/video","/REST/**","/per","/param").hasAnyAuthority( "USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/admin/**").hasAuthority( "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/admin/*").hasAuthority( "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/").hasAnyAuthority( "USER", "ADMIN")
                        .dispatcherTypeMatchers(ERROR).permitAll()

                        .anyRequest().denyAll()
                )

                //.httpBasic(withDefaults())
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
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
                                .maximumSessions(1)
                                .sessionRegistry(sessionRegistry())
                )

                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
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

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}