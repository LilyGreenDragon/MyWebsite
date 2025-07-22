package org.spring.MySite.config;


import org.modelmapper.ModelMapper;
import org.spring.MySite.security.*;
import org.spring.MySite.services.OAuth2PeopleService;
import org.spring.MySite.services.PersonDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.web.context.request.RequestContextListener;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static jakarta.servlet.DispatcherType.ERROR;


@ComponentScan(basePackages = { "org.spring.MySite" })
@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfig {

    private PersonDetailsService personDetailsService;
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    private CustomAuthenticationFailureHandler authenticationFailureHandler;
    private CustomAccessDeniedHandler accessDeniedHandler;
    private OAuth2PeopleService oAuth2PeopleService;
    private OAuth2AuthorizedClientService authorizedClientService;
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;



    @Autowired
    public SecurityConfig(PersonDetailsService personDetailsService, CustomAuthenticationSuccessHandler authenticationSuccessHandler, CustomAuthenticationFailureHandler authenticationFailureHandler,
                          CustomAccessDeniedHandler accessDeniedHandler, OAuth2PeopleService oAuth2PeopleService, OAuth2AuthorizedClientService authorizedClientService, OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler) {
        this.personDetailsService = personDetailsService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.oAuth2PeopleService = oAuth2PeopleService;
        this.authorizedClientService = authorizedClientService;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
}

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //.cors(withDefaults())
                //.csrf(AbstractHttpConfigurer::disable)

                /*.addFilterBefore(new RequestLoggerFilter(),
                BasicAuthenticationFilter.class)*/
                /*.addFilterBefore(new ResponseFilter(),
                        ChannelProcessingFilter.class)*/
                .addFilterBefore(new UsernamePasswordAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((requests) -> requests

                        .requestMatchers(HttpMethod.GET,"/", "/error","/js/**", "/css/**","/images/**","/imagecab/**", "/login", "/register","/userIsAbsent","/access-denied", "/oauth2/**", "/expiredSession","/oauth2/password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/register", "/login", "/oauth2/password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/home","/myPage","/myPage/photo", "/myPage/mail", "/makeDinner", "/myPage/photo/delete", "/REST/**").hasAnyAuthority( "USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/home", "/myPage", "/photo", "/news", "/holiday","/makeDinner", "/admin/adminIn","/myPage/photo","/dr2021","/video","/REST/**","/per","/param","/session").hasAnyAuthority( "USER", "ADMIN")
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/").hasAnyAuthority( "USER", "ADMIN")
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
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .authorizedClientService(authorizedClientService)
                        .userInfoEndpoint(userInfo -> userInfo
                        .userService(oAuth2PeopleService))
                        .defaultSuccessUrl("/",true)
                        //.failureUrl("/error?error=Login failed")
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .logout((logout) -> logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .logoutUrl("/logout")
                        //.logoutSuccessUrl("/")
                        .logoutSuccessHandler(logoutSuccessHandler()) // Обработчик для OAuth2
                        .permitAll()
                )

                .sessionManagement(session ->
                        session
                                //.sessionFixation().none()
                                //.invalidSessionUrl("/invalidSession.html")
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                .sessionFixation().migrateSession() // Без разрыва сессии при смене сервера
                                .maximumSessions(1)
                                .expiredSessionStrategy(new CustomSessionExpiredStrategy())
                                //.maxSessionsPreventsLogin(true) // Блокировать новые входы
                                .sessionRegistry(sessionRegistry())
                )

        .build();
    }


  /*  @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user =
                User.withDefaultPasswordEncoder()
                        .username("user")
                        .password("password")
                        .roles("USER")
                        .build();

        UserDetails admin =
                User.withDefaultPasswordEncoder()
                        .username("admin")
                        .password("password")
                        .roles("ADMIN")
                        .build();

        return new InMemoryUserDetailsManager(user,admin);
    }*/


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
    return authenticationConfiguration.getAuthenticationManager();
    }
/*
    @Bean
    public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
}
*/
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
    @Bean
    public RequestContextListener requestContextListener(){
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
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
                // Для OAuth2 пользователей - редирект на кастомный обработчик
                response.sendRedirect("/oauth2/logout");
            } else {
                // Для обычных пользователей
                response.sendRedirect("/");
            }
        };
    }


    /*@Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }*/

   /* @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(encoder());
    }*/
/*
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
*/

}
