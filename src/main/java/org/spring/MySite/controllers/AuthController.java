package org.spring.MySite.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.spring.MySite.DTO.LoginDTO;
import org.spring.MySite.DTO.RegisterDTO;

import org.spring.MySite.models.Dictionary;
import org.spring.MySite.repositories.DictionaryRepository;
import org.spring.MySite.services.PeopleService;
import org.spring.MySite.services.RegistrationAttemptService;
import org.spring.MySite.services.RegistrationService;
import org.spring.MySite.util.PersonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
//@RequestMapping("/auth")
public class AuthController {
    private AuthenticationManager authenticationManager;
    private RegistrationService registrationService;
    private PersonValidator personValidator;
    private PeopleService peopleService;

   // @Autowired
   // private SessionRegistry sessionRegistry;

    //private JWTUtil jwtUtil;

    @Autowired
    private MessageSource messages;

    @Autowired
    private RegistrationAttemptService registrationAttemptService;

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    String flag;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, RegistrationService registrationService,
                          PersonValidator personValidator, PeopleService peopleService) {
        this.authenticationManager = authenticationManager;
        this.registrationService = registrationService;
        this.personValidator = personValidator;
        this.peopleService = peopleService;
    }

    @GetMapping("/login")
    public String login(Model model, @RequestParam("error") final Optional<String> error, Authentication authentication) {

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("loginDTO", new LoginDTO());
            error.ifPresent(e -> model.addAttribute("error", e));
            return "login";
        }
        return "authenticatedUser";
    }

    //Кривые ручки
    @GetMapping("/userIsAbsent")
    public String loginError(Model model) {
        return "userIsAbsent";
    }

    @GetMapping("/register")
    public String register(Model model, @RequestParam("messageKey" ) final Optional<String> messageKey,HttpServletRequest request, Authentication authentication) {
        //System.out.println("http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath());
        Locale locale = request.getLocale();
        messageKey.ifPresent( key -> {
                    String message = messages.getMessage("auth.message.blocked", null, locale);
                    model.addAttribute("message", message);
                });

            if (authentication == null || authentication instanceof AnonymousAuthenticationToken /*|| authentication instanceof OAuth2AuthenticationToken*/) {
                model.addAttribute("registerDTO",new RegisterDTO());
                return "register";}

            return "authenticatedUser";
    }

    @PostMapping("/register")
    public String register( @ModelAttribute("registerDTO") @Valid RegisterDTO registerDTO, BindingResult bindingResult, Model model,  HttpServletRequest request, HttpServletResponse response) {

        if (registrationAttemptService.isBlocked()) {
            //throw new RuntimeException("blocked");
            return "redirect:/register?messageKey";
        }
        personValidator.validate(registerDTO, bindingResult);
        if(bindingResult.hasErrors()) {
            return "register";}
        Dictionary dictionary = dictionaryRepository.findById("password").get();
        if(!(registerDTO.getPasswordReg().equals(dictionary.getMeaning()))){
            final String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
                registrationAttemptService.registrationFailed(request.getRemoteAddr());
            } else {
                registrationAttemptService.registrationFailed(xfHeader.split(",")[0]);
            }
            return "badPassword";
        }
        else {
           /* request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();*/
            registrationAttemptService.registrationSucceeded(request.getRemoteAddr());

            String password= registerDTO.getPassword();
            registrationService.register(registerDTO);

            authWithHttpServletRequest(request, registerDTO.getUsername(), password);
            //authWithHttpServletRequest(request, registerDTO.getUsername(), registerDTO.getPassword());
            //return "forward:/login";
            return "redirect:/home";
        }
    }

    @GetMapping("/oauth2/password")
    public String showAdditionalPasswordForm(Model model) {
        model.addAttribute("registerDTO",new RegisterDTO());
        return "password";
    }

    @PostMapping("/oauth2/password")
    public String checkPassword( @ModelAttribute("registerDTO") RegisterDTO registerDTO,
            Authentication authentication, HttpServletRequest request, HttpServletResponse response) {

        Dictionary dictionary = dictionaryRepository.findById("password").get();
        if (!(registerDTO.getPasswordReg().equals(dictionary.getMeaning()))) {

            final String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
                registrationAttemptService.registrationFailed(request.getRemoteAddr());
            } else {
                registrationAttemptService.registrationFailed(xfHeader.split(",")[0]);
            }

            if (authentication instanceof OAuth2AuthenticationToken ) {
                //invalidateAllSessions((OAuth2User) authentication.getPrincipal());
                deleteSessions((OAuth2User) authentication.getPrincipal());
            }
            SecurityContextHolder.clearContext();
            new SecurityContextLogoutHandler().logout(request, response, authentication);

            return "badPasswordOAuth";
        } else {
            registrationAttemptService.registrationSucceeded(request.getRemoteAddr());
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER"));
                OAuth2AuthenticationToken authenticationUser = new OAuth2AuthenticationToken(
                        oAuth2User,
                        authorities,
                        "github");

                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(authenticationUser);
                //request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", context);
                request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
                String passwordForOAuthGitHub = "OAuth";
                String username = oAuth2User.getAttribute("login");
                String email = oAuth2User.getAttribute("OAUTH2_EMAIL");
                System.out.println("email " + email);
                RegisterDTO userDTO = new RegisterDTO(username, passwordForOAuthGitHub, email, registerDTO.getPasswordReg());
                registrationService.register(userDTO);

                return "redirect:/";
            }

            return "redirect:/login";
        }
    }


    public void authWithHttpServletRequest(HttpServletRequest request, String username, String password) {

        try {
            request.login(username, password);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String sessionId = request.getSession().getId();
            //sessionRegistry.registerNewSession(sessionId, authentication.getPrincipal()); //метод для SessionRegistry

        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    public void deleteSessions(OAuth2User oAuth2User) {
        String username = oAuth2User.getAttribute("login");
        Map<String, ? extends Session> userSessions = sessionRepository.findByPrincipalName(username);

        for (String sessionId : userSessions.keySet()) {
            sessionRepository.deleteById(sessionId);
        }
    }

/* Метод для SessionRegistry
    private void invalidateAllSessions(OAuth2User oAuth2User) {
        List<SessionInformation> sessions = sessionRegistry.getAllSessions(oAuth2User, false);
        for (SessionInformation session : sessions) {
            session.expireNow();
            sessionRegistry.removeSessionInformation(session.getSessionId());
        }
    }
*/
/*
    private void invalidateAllSessions(OAuth2User oAuth2User) {
        String username = oAuth2User.getAttribute("login");
        Set<Object> sessionIds = redisSessionRegistry.getSessions(username);

        if (sessionIds != null) {
            for (Object sessionId : sessionIds) {
                sessionRepository.deleteById(sessionId.toString()); // Удаляем сессию из Redis
            }
            redisSessionRegistry.clearAllSessions(username); // Чистим Redis SET
        }
    }
*/

}
