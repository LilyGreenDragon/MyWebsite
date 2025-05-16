package org.spring.MySite.util;

import org.spring.MySite.DTO.RegisterDTO;
import org.spring.MySite.repositories.PeopleRepository;

import org.spring.MySite.services.PeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PersonValidator implements Validator {

    @Autowired
    private PeopleService peopleService;

    @Override
    public boolean supports(Class<?> aClass) {
        return RegisterDTO.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        RegisterDTO registerDTO = (RegisterDTO) o;

        if(peopleService.findByEmail(registerDTO.getEmail()).isPresent()) {
            errors.rejectValue("email", "", "This email is already taken");
        }

        if(peopleService.findByUsername(registerDTO.getUsername()).isPresent()) {
            errors.rejectValue("username", "", "This nickname is already taken");
        }


    }
}
