package org.spring.MySite.services;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.spring.MySite.DTO.RegisterDTO;
import org.spring.MySite.models.Person;
import org.spring.MySite.repositories.PeopleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
//@Transactional(readOnly = true)
public class PeopleService {

    @Autowired
    private PeopleRepository peopleRepository;

    public Optional<Person> findByUsername(String username) {
        return peopleRepository.findByUsername(username);
    }

    public Person findById(int id) {
        Optional<Person> foundPerson = peopleRepository.findById(id);
        return foundPerson.orElse(null);
    }

  /*  public Person findById(int id) {
        return peopleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));
    } */

    public Optional<Person> findByEmail(String email) {
        return peopleRepository.findByEmail(email);
    }

    public List<Person> findAll() {
        return peopleRepository.findAll();
    }

    public void deleteById(int id) {
        peopleRepository.deleteById(id);
    }

    public void save(Person person) {
        peopleRepository.save(person);
    }

}
