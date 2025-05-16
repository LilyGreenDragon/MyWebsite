package org.spring.MySite.services;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.spring.MySite.models.Person;
import org.spring.MySite.repositories.PeopleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//Частичный интеграционный тест (sliced test)
//Проверяет корректность работы Spring DI (внедрение зависимостей), интеграцию между реальным бином PeopleService и мокированным репозиторием, что Spring-прокси и AOP работают корректно
@SpringBootTest  // Загружает контекст Spring Boot
public class PeopleServiceIntegrationTestWithMock {

    @Autowired
    private PeopleService peopleService;  // Реальный бин Spring

    @MockBean
    private PeopleRepository peopleRepository;  // Мок, интегрированный с Spring

    @Test
    public void givenPersonWithValidId_whenFindById_thenReturnPerson() {

        when(peopleRepository.findById(1)).thenReturn(Optional.of(new Person(1,"Andre", "55555", "andre@mail.ru")));
        //Optional<Person> mockResult = peopleRepository.findById(1);
        //System.out.println("Mock returns: " + mockResult);

        Person person = peopleService.findById(1);
        System.out.println("Service returns: " + person);

        assertEquals("Andre", person.getUsername());
        assertEquals("andre@mail.ru", person.getEmail());
        verify(peopleRepository, times(1)).findById(1);
    }

    @Test
    public void givenInvalidId_whenFindById_thenThrowException() {
        when(peopleRepository.findById(1)).thenReturn(Optional.empty());
        Person person = peopleService.findById(1);
        assertNull(person);

        verify(peopleRepository, times(1)).findById(1);
    }



}
