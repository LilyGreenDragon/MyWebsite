package org.spring.MySite.services;

import org.spring.MySite.models.Lesson;
import org.spring.MySite.repositories.LessonsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LessonsService {

    @Autowired
    private LessonsRepository lessonsRepository;

    public Lesson findById(Long id) {
        Optional<Lesson> foundLesson = lessonsRepository.findById(id);
        return foundLesson.orElse(null);
    }
}
