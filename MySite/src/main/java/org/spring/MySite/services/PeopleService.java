package org.spring.MySite.services;

import org.spring.MySite.models.Person;
import org.spring.MySite.repositories.PeopleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PeopleService {

    @Autowired
    PeopleRepository peopleRepository;

    public Person findOne(String username) {
        Optional<Person> foundPerson = peopleRepository.findByUsername(username);
        return foundPerson.orElse(null);
    }

}
