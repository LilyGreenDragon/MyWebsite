package org.spring.MySite.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
//@EnableWebFluxSecurity
@Profile("testWebTestClient")
public class TestSecurityConfigWebTestClient {
/*
    @Autowired
    private PersonDetailsService personDetailsService;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .addFilterAt(testAuthWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/REST/myPage").hasAnyAuthority("USER", "ADMIN")
                        //.pathMatchers("/REST/myPage").permitAll()
                        //.pathMatchers("/REST/myPage").hasAuthority("BLOCKED")
                        .anyExchange().permitAll()
                );
                //.userDetailsService(personDetailsService)
                //.httpBasic(Customizer.withDefaults());


        return http.build();
    }

    @Bean
    public TestAuthWebFilter testAuthWebFilter() {
        return new TestAuthWebFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        //return new BCryptPasswordEncoder();
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }


*/
}
