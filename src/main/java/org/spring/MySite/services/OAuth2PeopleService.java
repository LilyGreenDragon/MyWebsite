package org.spring.MySite.services;

import org.spring.MySite.DTO.RegisterDTO;
import org.spring.MySite.models.PasswordIn;
import org.spring.MySite.models.Person;
import org.spring.MySite.models.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OAuth2PeopleService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private PeopleService peopleService;
    private RolesService rolesService;
    private RegistrationService registrationService;
    private PasswordEncoder passwordEncoder;
    private RegistrationAttemptService registrationAttemptService;

    @Autowired
    public OAuth2PeopleService(PeopleService peopleService, RolesService rolesService, RegistrationService registrationService,
                               PasswordEncoder passwordEncoder, RegistrationAttemptService registrationAttemptService) {
        this.peopleService = peopleService;
        this.rolesService = rolesService;
        this.registrationService = registrationService;
        this.passwordEncoder = passwordEncoder;
        this.registrationAttemptService = registrationAttemptService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException{
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(request);

        String passwordForOAuthGitHub="OAuth";
        System.out.println("Attributes " + oAuth2User.getAttributes());
        String username = oAuth2User.getAttribute("login");
        String email = oAuth2User.getAttribute("email");

        if (registrationAttemptService.isBlocked()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("user_blocked"),
                    "Too many attempts, please try again later"
            );
        }

        if (email==null) {
            email=fetchEmail(request);
            if (email==null) {
                throw new OAuth2AuthenticationException(new OAuth2Error("no_verified_email"),"You must have at least one verified email");
            }
        }

        Optional<Person> person = peopleService.findByUsername(username);
        System.out.println(person);

        if (person.isEmpty()) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                requestAttributes.setAttribute(
                        "OAUTH2_EMAIL",
                        email,
                        RequestAttributes.SCOPE_SESSION);
            }

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("newOAuth2"));
            System.out.println("authorities "+authorities);
           return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "login");

        } else if(!passwordEncoder.matches(passwordForOAuthGitHub,person.get().getPassword())){
            throw new OAuth2AuthenticationException(new OAuth2Error("username_is_taken"),"This username is already taken");
        } else if (passwordEncoder.matches(passwordForOAuthGitHub,person.get().getPassword())){
            System.out.println(person.get().getRoles().get(0));
            Role role = person.get().getRoles().get(0);
            if(role.getName().equals("BLOCKED")){
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("BLOCKED"));
                return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "login");
            }
            if(role.getName().equals("ADMIN")){
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ADMIN"));
                return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "login");
            }

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER"));
            return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "login");

        }

        throw new OAuth2AuthenticationException(new OAuth2Error("unexpected_exception"),"Unexpected exception");
    }

    public String fetchEmail(OAuth2UserRequest request) {
        String uri = "https://api.github.com/user/emails";
        String accessToken = request.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> emails = response.getBody();
        if (emails != null) {
            for (Map<String, Object> emailEntry : emails) {
                Boolean isPrimary = (Boolean) emailEntry.get("primary");
                Boolean isVerified = (Boolean) emailEntry.get("verified");
                if (Boolean.TRUE.equals(isPrimary) && Boolean.TRUE.equals(isVerified)) {
                    return (String) emailEntry.get("email");
                }
            }
            for (Map<String, Object> emailEntry : emails) {
                Boolean isVerified = (Boolean) emailEntry.get("verified");
                if (Boolean.TRUE.equals(isVerified)) {
                    return (String) emailEntry.get("email");
                }
            }

        }

        return null;
    }

}



//уже после лог ина может изменить логин в гитхабе