package org.spring.MySite.controllers;

import org.spring.MySite.models.PasswordIn;
import org.spring.MySite.models.Person;
import org.spring.MySite.models.Role;
import org.spring.MySite.repositories.PeopleRepository;
import org.spring.MySite.repositories.RolesRepository;
import org.spring.MySite.security.PD;
import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.PeopleService;
import org.spring.MySite.services.RolesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private SessionRegistry sessionRegistry;
    private RolesService rolesService;
    private PeopleService peopleService;

    @Autowired
    public AdminController(SessionRegistry sessionRegistry, RolesService rolesService, PeopleService peopleService) {
        this.sessionRegistry = sessionRegistry;
        this.rolesService = rolesService;
        this.peopleService = peopleService;
    }

    @GetMapping("/adminIn")
    public String adminIn(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals("ADMIN")) {

                return "redirect:/admin/admin";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/admin")
    public String adminPage(Model model, @ModelAttribute("person") Person person, @ModelAttribute("passwordIn") PasswordIn passwordIn) {

        List<Person> people = peopleService.findAll();
        Map<Integer, String> rolesMap = people.stream()
                .collect(Collectors.toMap(
                        Person::getId,
                        p -> p.getRoles().get(0).getName()
                ));
        model.addAttribute("people", peopleService.findAll());
        model.addAttribute("peopleLogged", findAllLoggedUsers());
        model.addAttribute("rolesMap", rolesMap);
        model.addAttribute("pass",passwordIn);

        return "adminPage";
    }

    /*@PostMapping("/admin")
    public String makeAdmin(@ModelAttribute("person") Person person) {
        System.out.println(person.getUsername());
        return "redirect:/admin";
    }*/

    @PostMapping("/block")
    public String blockUser (@ModelAttribute("person") Person person) {

        Person updatedPerson=peopleService.findByUsername(person.getUsername()).get();
        System.out.println(updatedPerson);
        Role role = rolesService.findByName("BLOCKED").get();
        updatedPerson.getRoles().clear();
        updatedPerson.getRoles().add(role);
        peopleService.save(updatedPerson);
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

        return "redirect:/admin/admin";
    }

    @PostMapping("/makeUser")
    public String makeUser (@ModelAttribute("person") Person person) {

        Person updatedPerson=peopleService.findByUsername(person.getUsername()).get();
        System.out.println(updatedPerson);
        Role role = rolesService.findByName("USER").get();
        updatedPerson.getRoles().clear();
        updatedPerson.getRoles().add(role);
        peopleService.save(updatedPerson);
        return "redirect:/admin/admin";
    }

    @PostMapping("/makeAdm")
    public String makeAdm (@ModelAttribute("person") Person person) {

        Person updatedPerson=peopleService.findByUsername(person.getUsername()).get();
        System.out.println(updatedPerson);
        Role role = rolesService.findByName("ADMIN").get();
        updatedPerson.getRoles().clear();
        updatedPerson.getRoles().add(role);
        peopleService.save(updatedPerson);
        return "redirect:/admin/admin";
    }

    @DeleteMapping("/delete")
    public String deleteUser (@ModelAttribute("person") Person person) {

        Person deletedPerson=peopleService.findByUsername(person.getUsername()).get();
        System.out.println(deletedPerson);
       // peopleService.deleteById(deletedPerson.getId());

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

        peopleService.deleteById(deletedPerson.getId());
        return "redirect:/admin/admin";
    }

    @GetMapping("/logUsers")
    public String admin(Model model) {
        model.addAttribute("people", findAllLoggedUsers());
        List<Object> userSessions = sessionRegistry.getAllPrincipals();
        System.out.println(userSessions);
        return "users";
    }

    public List<Person> findAllLoggedUsers() {
        return sessionRegistry.getAllPrincipals()
                .stream()
                .filter(principal -> principal instanceof PersonDetails)
                .map(PersonDetails.class::cast)
                .map(PersonDetails::getPerson)
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
        model.addAttribute("person", peopleService.findByUsername(username).get());

        return "viewUser";
    }

    @PostMapping("/pass")
    public String passwordReg(@ModelAttribute("pass") PasswordIn passwordIn) {
PasswordIn passIn= new PasswordIn();
passIn.setPasswordReg(passwordIn.getPasswordReg());
passIn.print();
        return "redirect:/admin/admin";
    }
}


