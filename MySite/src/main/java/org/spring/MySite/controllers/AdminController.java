package org.spring.MySite.controllers;

import org.spring.MySite.models.PasswordIn;
import org.spring.MySite.models.Person;
import org.spring.MySite.models.Role;
import org.spring.MySite.repositories.PeopleRepository;
import org.spring.MySite.repositories.RolesRepository;
import org.spring.MySite.security.PD;
import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.PeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    SessionRegistry sessionRegistry;
    PeopleRepository peopleRepository;
    RolesRepository rolesRepository;
    PeopleService peopleService;

    @Autowired
    public AdminController(SessionRegistry sessionRegistry, PeopleRepository peopleRepository, RolesRepository rolesRepository, PeopleService peopleService) {
        this.sessionRegistry = sessionRegistry;
        this.peopleRepository = peopleRepository;
        this.rolesRepository = rolesRepository;
        this.peopleService = peopleService;
    }

    @GetMapping("/adminIn")
    public String adminIn(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals("ADMIN")) {

                return "redirect:/admin";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/admin")
    public String adminPage(Model model, @ModelAttribute("person") Person person, @ModelAttribute("passwordIn") PasswordIn passwordIn) {

        model.addAttribute("people", peopleRepository.findAll());
        model.addAttribute("peopleLogged", findAllLoggedInUsers());
        model.addAttribute("pass",passwordIn);

        return "adminPage";
    }

    @PostMapping("/block")
    public String blockUser (@ModelAttribute("person") Person person) {

        Person updatedPerson=peopleService.findOne(person.getUsername());
        System.out.println(updatedPerson);
        Role roles = rolesRepository.findByName("BLOCKED").get();
        updatedPerson.getRoles().clear();
        updatedPerson.getRoles().add(roles);

        peopleRepository.save(updatedPerson);

       List<Object> principals = sessionRegistry.getAllPrincipals();
        for (Object principal : principals) {
            if (principal instanceof PersonDetails) {
                PersonDetails user = (PersonDetails) principal;
                if (user.getPerson().getUsername().equals(person.getUsername())){
                //if (user.getPerson().getId()==person.getId())

                    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                    for (SessionInformation session : sessions) {
                        session.expireNow(); // invalidate the session
                    }
                    System.out.println(((PersonDetails) principal).getAuthorities());
                }
            }
        }
        return "redirect:/admin";
    }

    @PostMapping("/unblock")
    public String unblockUser (@ModelAttribute("person") Person person) {

        Person updatedPerson=peopleService.findOne(person.getUsername());
        System.out.println(updatedPerson);
        Role roles = rolesRepository.findByName("USER").get();
        updatedPerson.getRoles().clear();
        updatedPerson.getRoles().add(roles);

        peopleRepository.save(updatedPerson);

        return "redirect:/admin";
    }

    @GetMapping("/logUsers")
    public String admin(Model model) {
        model.addAttribute("people", findAllLoggedInUsers());
        List<Object> userSessions = sessionRegistry.getAllPrincipals();
        System.out.println(userSessions);
        return "users";
    }

    public List<PersonDetails> findAllLoggedInUsers() {
        return sessionRegistry.getAllPrincipals()
                .stream()
                .filter(principal -> principal instanceof PersonDetails)
                .map(PersonDetails.class::cast)
                .collect(Collectors.toList());
    }

    @GetMapping("/loggedUsers")
    public String getLoggedUsersFromSessionRegistry(final Locale locale, final Model model) {
        model.addAttribute("people", getUsersFromSessionRegistry());

        List<Object> userSessions = sessionRegistry.getAllPrincipals();
        System.out.println(userSessions);
        return "users";
    }

    public List<String> getUsersFromSessionRegistry() {
        return sessionRegistry.getAllPrincipals()
                .stream()
                .filter((u) -> !sessionRegistry.getAllSessions(u, false).isEmpty())
                .map(o -> {
                    if (o instanceof PersonDetails) {
                        return ((PersonDetails) o).getUsername();
                    } else {
                        return o.toString()
                                ;
                    }
                }).collect(Collectors.toList());
    }

    @GetMapping("/user/{username}")
    public String userInfo(Model model, @PathVariable("username") String username) {
        model.addAttribute("person", peopleService.findOne(username));

        return "viewUser";
    }

    @PostMapping("/pass")
    public String passwordReg(@ModelAttribute("pass") PasswordIn passwordIn) {
PasswordIn passIn= new PasswordIn();
passIn.setPasswordReg(passwordIn.getPasswordReg());
passIn.print();
        return "redirect:/admin";
    }
}


