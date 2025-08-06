package org.spring.MySite.config;



import jakarta.servlet.http.HttpSession;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.context.request.RequestContextListener;
import jakarta.servlet.http.Cookie;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static jakarta.servlet.DispatcherType.ERROR;


@ComponentScan(basePackages = {"org.spring.MySite"})
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
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    //private RedisIndexedSessionRepository sessionRepository;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public SecurityConfig(PersonDetailsService personDetailsService, CustomAuthenticationSuccessHandler authenticationSuccessHandler, CustomAuthenticationFailureHandler authenticationFailureHandler,
                          CustomAccessDeniedHandler accessDeniedHandler, OAuth2PeopleService oAuth2PeopleService, OAuth2AuthorizedClientService authorizedClientService, OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
                          OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) {
        this.personDetailsService = personDetailsService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.oAuth2PeopleService = oAuth2PeopleService;
        this.authorizedClientService = authorizedClientService;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
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

                        .requestMatchers(HttpMethod.GET,"/", "/error","/js/**", "/css/**","/images/**","/imagecab/**", "/login", "/register","/userIsAbsent","/access-denied", "/oauth2/**", "/expiredSession", "/blockedUser").permitAll()
                        .requestMatchers(HttpMethod.POST, "/register", "/login", "/oauth2/password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/home","/myPage","/myPage/photo", "/myPage/mail", "/makeDinner", "/myPage/photo/delete", "/REST/**").hasAnyAuthority( "USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET,"/home", "/myPage", "/photo", "/news", "/holiday","/makeDinner", "/admin/adminIn","/myPage/photo","/dr2021","/video","/REST/**","/per","/param","/session").hasAnyAuthority( "USER", "ADMIN")
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/").hasAnyAuthority( "USER", "ADMIN")
                        .dispatcherTypeMatchers(ERROR).permitAll()
                        .anyRequest().denyAll()
                )
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
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        //.logoutSuccessHandler(logoutSuccessHandler()) // Обработчик для OAuth2
                        .permitAll()
                )
                .sessionManagement(session ->
                        session
                                //.sessionFixation().none()
                                //.invalidSessionUrl("/invalidSession.html")
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                .sessionFixation().migrateSession() // Без разрыва сессии при смене сервера
                                //.maximumSessions(1)
                                //.expiredSessionStrategy(new CustomSessionExpiredStrategy())
                                //.maxSessionsPreventsLogin(true) // Блокировать новые входы
                                //.sessionRegistry(sessionRegistry())
                )

        .build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
    return authenticationConfiguration.getAuthenticationManager();
    }
    //В UtilConfig
/*
    @Bean
    public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
}
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
*/
/*
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

//чтобы Spring Security получал уведомления о создании и уничтожении HTTP-сессий
//Удаление сессии (например, при logout или вытеснении при maximumSessions) не приведёт к удалению её из SessionRegistry без этого бина
    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
    }

//Регистрация сессии в SessionRegistry после логина
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(SessionRegistry sessionRegistry) {
        return new RegisterSessionAuthenticationStrategy(sessionRegistry);
    }
    */
/*
    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        ServletListenerRegistrationBean<HttpSessionEventPublisher> listenerRegBean =
                new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
        listenerRegBean.setOrder(1); // порядок инициализации (опционально)
        return listenerRegBean;
    }
*/
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

/* если используется SessionRegistry
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
    */
/*
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String sessionId = session.getId();
                System.out.println("sessionId "+sessionId );
                sessionRepository.deleteById(sessionId); // Удаляем сессию из Redis
                redisTemplate.expire("spring:session:sessions:" + sessionId, Duration.ofSeconds(1));
                // Инвалидируем вручную
                session.invalidate();
            }

            // Удаление JSESSIONID cookie
            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            // Очистка SecurityContext, если не сделана автоматически
            new SecurityContextLogoutHandler().logout(request, response, authentication);
                response.sendRedirect("/");
        };
    }
*/

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

}
