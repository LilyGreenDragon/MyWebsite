package org.spring.MySite.controllers;

import org.spring.MySite.models.PasswordIn;
import org.spring.MySite.models.Person;
import org.spring.MySite.models.Role;

import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.PeopleService;
import org.spring.MySite.services.RolesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private RolesService rolesService;
    private PeopleService peopleService;

    //@Autowired
    //private SessionRegistry sessionRegistry;

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AdminController(RolesService rolesService, PeopleService peopleService) {
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
        System.out.println("updatedPerson " +updatedPerson);
        Role role = rolesService.findByName("BLOCKED").get();
        updatedPerson.getRoles().clear();
        updatedPerson.getRoles().add(role);
        peopleService.save(updatedPerson);

        //expireSession(person);
        deleteSession(person);
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

        //expireSession(person);
        deleteSession(person);
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

        //expireSession(person);
        deleteSession(person);
        return "redirect:/admin/admin";
    }

    @DeleteMapping("/delete")
    public String deleteUser (@ModelAttribute("person") Person person) {

        Person deletedPerson=peopleService.findByUsername(person.getUsername()).get();
        //expireSession(person);
        deleteSession(person);
        peopleService.deleteById(deletedPerson.getId());
        return "redirect:/admin/admin";
    }
/*
    @GetMapping("/logUsers")
    public String admin(Model model) {
        model.addAttribute("people", findAllLoggedUsers());

        List<Object> userSessions = sessionRegistry.getAllPrincipals();
        System.out.println(userSessions);
        return "users";
    }
//здесь не учтено что может быть principal instanceof oAuthUser
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

//здесь не учтено что может быть principal instanceof oAuthUser
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
*/


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

    @PostMapping("/cleanSessionIndexes")
    public String cleanSessionIndexes() {
        cleanFalseSessionIndexes();
        return "redirect:/admin/admin";
    }

    public void cleanFalseSessionIndexes() {
        System.out.println("Enter clean");
        String indexPrefix = "spring:session:index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:";

        Set<String> indexKeys = redisTemplate.keys(indexPrefix + "*");
        System.out.println(indexKeys);
        if (indexKeys == null) return;

        for (String indexKey : indexKeys) {
            System.out.println("indexKey "+indexKey);
            Set<Object> sessionIds = redisTemplate.opsForSet().members(indexKey);
            if (sessionIds == null) continue;
            System.out.println("sessionIds "+sessionIds);

            for (Object rawSessionId : sessionIds) {
                String sessionId = null;

                if (rawSessionId instanceof byte[] bytes) {
                    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                        Object obj = ois.readObject();
                        if (obj instanceof String str) {
                            sessionId = str;
                            System.out.println("sessionId "+ sessionId);
                        }
                    } catch (Exception e) {
                        System.err.println("Ошибка десериализации sessionId: " + e.getMessage());
                        continue;
                    }
                }
                if (rawSessionId instanceof String) {
                  sessionId = (String)rawSessionId;
                    System.out.println("sessionId "+ sessionId);
                }

                if (sessionId == null) continue;

                String sessionKey = "spring:session:sessions:" + sessionId;

                if (!Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
                    // Сессии нет — удаляем ID из индекса
                    redisTemplate.opsForSet().remove(indexKey, rawSessionId);
                    System.out.println("Удален sessionId " + sessionId + " из индекса " + indexKey);
                }
            }

            Long size = redisTemplate.opsForSet().size(indexKey);
            if (size != null && size == 0) {
                redisTemplate.delete(indexKey);
                System.out.println("Удален пустой индексный ключ: " + indexKey);
            }
        }
    }


    public List<Person> findAllLoggedUsers() {
        String indexPrefix = "spring:session:index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:";
        Set<String> indexKeys = redisTemplate.keys(indexPrefix + "*");

        if (indexKeys == null) {
            return Collections.emptyList();
        }

        Set<Person> persons = new HashSet<>();

        for (String indexKey : indexKeys) {
            Set<Object> sessionIds = redisTemplate.opsForSet().members(indexKey);

            if (sessionIds == null || sessionIds.isEmpty()) continue;

            for (Object sessionIdObj : sessionIds) {
                String sessionId = String.valueOf(sessionIdObj);
                String sessionKey = "spring:session:sessions:" + sessionId;

                Object contextObj = redisTemplate.opsForHash().get(sessionKey, "sessionAttr:SPRING_SECURITY_CONTEXT");

                if (contextObj instanceof SecurityContext context) {
                    Authentication auth = context.getAuthentication();
                    if (auth != null && auth.getPrincipal() instanceof PersonDetails personDetails) {
                        persons.add(personDetails.getPerson());
                        break; // нашли хотя бы одну валидную сессию — достаточно
                    }
                    if (auth != null && auth.getPrincipal() instanceof OAuth2User oAuth2User ) {
                        String username = oAuth2User.getAttribute("login");
                        persons.add(peopleService.findByUsername(username).get());
                        break; // нашли хотя бы одну валидную сессию — достаточно
                    }
                }
            }
        }

        return new ArrayList<>(persons);
    }

    public void deleteSession(Person person) {
        String username = person.getUsername();
        Map<String, ? extends Session> userSessions = sessionRepository.findByPrincipalName(username);

        for (String sessionId : userSessions.keySet()) {
            sessionRepository.deleteById(sessionId);
        }
    }

//метод для SessionRegistry
/*public void expireSession (Person person) {
    List<Object> principals = sessionRegistry.getAllPrincipals();
    for (Object principal : principals) {
        System.out.println("Принципал " +principal);
        if (principal instanceof PersonDetails) {
            PersonDetails user = (PersonDetails) principal;
            if (user.getPerson().getUsername().equals(person.getUsername())) {
                //if (user.getPerson().getId()==person.getId())
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                for (SessionInformation session : sessions) {
                    session.expireNow();
                }
                //System.out.println("Authorities " +((PersonDetails) principal).getAuthorities());
            }
        }
        if (principal instanceof OAuth2User user) {
            if (user.getName().equals(person.getUsername())) {
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                for (SessionInformation session : sessions) {
                    session.expireNow();
                }
            }
        }
    }
}
*/


}


