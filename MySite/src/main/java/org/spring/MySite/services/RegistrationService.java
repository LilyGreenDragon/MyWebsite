package org.spring.MySite.services;

import org.spring.MySite.DTO.RegisterDTO;
import org.spring.MySite.models.Person;
import org.spring.MySite.models.Role;
import org.spring.MySite.repositories.PeopleRepository;
import org.spring.MySite.repositories.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RegistrationService {
    private final PeopleRepository peopleRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationService(PeopleRepository peopleRepository, RolesRepository rolesRepository, PasswordEncoder passwordEncoder) {
        this.peopleRepository = peopleRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
    }

@Transactional
    public Person register(RegisterDTO registerDTO){

        Person person = new Person();
        person.setUsername(registerDTO.getUsername());
        person.setPassword(passwordEncoder.encode((registerDTO.getPassword())));
        person.setEmail(registerDTO.getEmail());

        Role roles = rolesRepository.findByName("USER").get();
        System.out.println(roles);

        person.getRoles().add(roles);
        //person.setRoles(Collections.singletonList(roles));

        peopleRepository.save(person);

        return person;
    }
}
