package org.spring.MySite.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.spring.MySite.DTO.LoginDTO;
import org.spring.MySite.DTO.RegisterDTO;

import org.spring.MySite.models.PasswordIn;
import org.spring.MySite.repositories.PeopleRepository;
import org.spring.MySite.services.PeopleService;
import org.spring.MySite.services.RegistrationAttemptService;
import org.spring.MySite.services.RegistrationService;
import org.spring.MySite.util.JWTUtil;
import org.spring.MySite.util.PersonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
//@RequestMapping("/auth")
public class AuthController {
    private AuthenticationManager authenticationManager;
    private RegistrationService registrationService;
    private PersonValidator personValidator;
    private PeopleService peopleService;
    private SessionRegistry sessionRegistry;
    //private JWTUtil jwtUtil;

    @Autowired
    private MessageSource messages;

    @Autowired
    private RegistrationAttemptService registrationAttemptService;

    String flag;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, RegistrationService registrationService, PersonValidator personValidator, PeopleService peopleService, SessionRegistry sessionRegistry) {
        this.authenticationManager = authenticationManager;
        this.registrationService = registrationService;
        this.personValidator = personValidator;
        this.peopleService = peopleService;
        this.sessionRegistry = sessionRegistry;

    }

    @GetMapping("/login")
    public String login(Model model, @RequestParam("error") final Optional<String> error) {

        model.addAttribute("loginDTO",new LoginDTO());
        error.ifPresent( e ->  model.addAttribute("error", e));
        return "login";
    }

    //Кривые ручки
    @GetMapping("/userIsAbsent")
    public String loginError(Model model) {
        return "userIsAbsent";
    }

    @GetMapping("/register")
    public String register(Model model, @RequestParam("messageKey" ) final Optional<String> messageKey,HttpServletRequest request) {
        //System.out.println("http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath());
        Locale locale = request.getLocale();
        messageKey.ifPresent( key -> {
                    String message = messages.getMessage("auth.message.blocked", null, locale);
                    model.addAttribute("message", message);
                });

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {

            if (authentication instanceof AnonymousAuthenticationToken) {
                model.addAttribute("registerDTO",new RegisterDTO());
                return "register";}

            return "blockPage";}

        model.addAttribute("registerDTO",new RegisterDTO());
        return "register";
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
        PasswordIn passIn = new PasswordIn();
        if(!(registerDTO.getPasswordReg().equals(passIn.getPasswordReg()))){
            final String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
                registrationAttemptService.registrationFailed(request.getRemoteAddr());
            } else {
                registrationAttemptService.registrationFailed(xfHeader.split(",")[0]);
            }
            return "badPassword";
        }
        else {
            request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();
            registrationAttemptService.registrationSucceeded(request.getRemoteAddr());

            String password=  registerDTO.getPassword();
            registrationService.register(registerDTO);

           // String token = jwtUtil.generateToken(registerDTO.getUsername());
           // Map<String,String> mapToken = Map.of("jwt-token", token);


            //не работает
            //authWithAuthManager(request, registerDTO.getUsername(), registerDTO.getPassword());
            //не работает
       /*UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(registerDTO.getUsername(), registerDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);*/

            //работает
            authWithHttpServletRequest(request, registerDTO.getUsername(), password);
            //authWithHttpServletRequest(request, registerDTO.getUsername(), registerDTO.getPassword());
            //return "forward:/login";
            return "redirect:/home";
        }
    }

    /*public void authWithAuthManager(HttpServletRequest request, String username, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        request.getSession();

        token.setDetails(new WebAuthenticationDetails(request));

        try{
            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            sessionRegistry.registerNewSession(request.getSession().getId(), auth.getPrincipal());
        } catch(Exception e){
            e.printStackTrace();
        }
    }*/

    public void authWithHttpServletRequest(HttpServletRequest request, String username, String password) {
        try {
            request.login(username, password);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            sessionRegistry.registerNewSession(request.getSession().getId(), auth.getPrincipal());
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

}
