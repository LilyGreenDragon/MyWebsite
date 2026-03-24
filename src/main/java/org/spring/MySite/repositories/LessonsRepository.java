package org.spring.MySite.repositories;

import org.spring.MySite.models.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonsRepository extends JpaRepository<Lesson, Long> {

    // Все занятия, отсортированные по дню и времени
    List<Lesson> findAllByOrderByDayOfWeekAscStartTimeAsc();
}
