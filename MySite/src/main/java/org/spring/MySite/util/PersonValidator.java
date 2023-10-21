package org.spring.MySite.util;

import org.spring.MySite.DTO.RegisterDTO;
import org.spring.MySite.repositories.PeopleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PersonValidator implements Validator {
    private final PeopleRepository peopleRepository;

    @Autowired
    public PersonValidator(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }


    @Override
    public boolean supports(Class<?> aClass) {
        return RegisterDTO.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        RegisterDTO registerDTO = (RegisterDTO) o;

        if(peopleRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            errors.rejectValue("email", "", "This email is already taken");
        }

        if(peopleRepository.findByUsername(registerDTO.getUsername()).isPresent()) {
            errors.rejectValue("username", "", "This nickname is already taken");
        }


    }
}
