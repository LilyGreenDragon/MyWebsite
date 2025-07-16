package org.spring.MySite.services;


import org.spring.MySite.models.Person;
import org.spring.MySite.models.Role;
import org.spring.MySite.repositories.PeopleRepository;
import org.spring.MySite.security.PersonDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
//Не использую
@Service
@Transactional
public class PersonDetailsService implements UserDetailsService {

    @Autowired
    private PeopleService peopleService;

    @Autowired
    private LoginAttemptService loginAttemptService;

   @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       System.out.println("IP клиента: " + loginAttemptService.getClientIP());
        if (loginAttemptService.isBlocked()) {
           throw new RuntimeException("blocked");
       }

        //Вариант 1
        /*Person person =  peopleService.findByUsername(username).get().orElseThrow(() ->new UsernameNotFoundException("User not found"));
        return new User(person.getUsername(), person.getPassword(), mapRolesToAuthorities(person.getRoles()));*/

       //Вариант 2
       Optional<Person> person = peopleService.findByUsername(username);

        if (person.isEmpty())
            throw new UsernameNotFoundException("User not found");

           return new PersonDetails(person.get());
    }

    private Collection<GrantedAuthority> mapRolesToAuthorities(List<Role> roles){
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }
}
