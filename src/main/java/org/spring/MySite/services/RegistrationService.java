package org.spring.MySite.services;

import org.modelmapper.ModelMapper;
import org.spring.MySite.DTO.RegisterDTO;
import org.spring.MySite.models.Person;
import org.spring.MySite.models.Role;
import org.spring.MySite.repositories.PeopleRepository;
import org.spring.MySite.repositories.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private PeopleService peopleService;
    private RolesService rolesService;
    private PasswordEncoder passwordEncoder;
    private ModelMapper modelMapper;

    @Autowired
    public RegistrationService(PeopleService peopleService, RolesService rolesService, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.peopleService = peopleService;
        this.rolesService = rolesService;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

@Transactional
    public Person register(RegisterDTO registerDTO){

        Person person=registerDTOToPerson(registerDTO);

       /* person.setUsername(registerDTO.getUsername());
        person.setPassword(passwordEncoder.encode((registerDTO.getPassword())));
        person.setEmail(registerDTO.getEmail());*/

        Role role = rolesService.findByName("USER").get();
        person.getRoles().add(role);
        //person.setRoles(Collections.singletonList(roles));
        peopleService.save(person);

        return person;
    }

    @Transactional
    public Person registerAdmin(RegisterDTO registerDTO){

        Person person=registerDTOToPerson(registerDTO);
        Role role = rolesService.findByName("ADMIN").get();
        person.getRoles().add(role);
        peopleService.save(person);

        return person;
    }

    public Person registerDTOToPerson(RegisterDTO registerDTO) {
        registerDTO.setPassword(passwordEncoder.encode((registerDTO.getPassword())));
        return modelMapper.map(registerDTO, Person.class);
    }
}
