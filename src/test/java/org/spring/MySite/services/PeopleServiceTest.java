package org.spring.MySite.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spring.MySite.models.Person;
import org.spring.MySite.repositories.PeopleRepository;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

//unit-тест
//Проверяет логику сервиса(метода findById()) в изоляции, корректность взаимодействия с репозиторием, не зависит от Spring-контекста
@ExtendWith(MockitoExtension.class)
public class PeopleServiceTest {

    @Mock
    private PeopleRepository peopleRepository;

    @InjectMocks
    private PeopleService peopleService;

    @Test
    void givenPerson_whenSave_thenSave() {
        //given
        Person mockPerson= new Person(88, "Rony", "1111", "rony@mail.ru");
        peopleService.save(mockPerson);

        // Проверяем, что personRepo.save() вызван 1 раз с этим person
        verify(peopleRepository).save(mockPerson);
    }

    @Test
    void givenPeople_whenFindAll_thenReturnAllPeople() {

        //given
        Person mockPerson1= new Person( 88, "Rony", "1111", "rony@mail.ru");
        Person mockPerson2= new Person(89, "Sony", "1111", "sony@mail.ru");
        //When
        when(peopleRepository.findAll())
                .thenReturn(List.of(mockPerson1,mockPerson2));
        List<Person>  personList = peopleService.findAll();
        //then
        assertThat(personList).isNotNull();
        assertThat(personList.size()).isEqualTo(2);
    }

    @Test
    void givenPerson_whenFindByUsername_thenReturnPerson() {

        //given
        Person mockPerson = new Person( 88, "Rony", "1111", "rony@mail.ru");
        when(peopleRepository.findByUsername("Rony"))
                .thenReturn(Optional.of(mockPerson));
        Optional<Person>  person = peopleService.findByUsername("Rony");
        // Assert
        assertTrue(person.isPresent());
        assertEquals("rony@mail.ru", person.get().getEmail());
        verify(peopleRepository, times(1)).findByUsername("Rony");
    }

    @Test
    void givenPerson_whenFindById_thenReturnPerson() {

        //given
        Person mockPerson = new Person( 88, "Rony", "1111", "rony@mail.ru");
        when(peopleRepository.findById(88))
                .thenReturn(Optional.of(mockPerson));
        Person  person = peopleService.findById(88);
        // Assert
        assertNotNull(person);
        assertEquals("rony@mail.ru", person.getEmail());
        verify(peopleRepository, times(1)).findById(88);
    }

}
