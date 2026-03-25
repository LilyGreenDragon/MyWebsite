package org.spring.MySite.repositories;

import org.spring.MySite.models.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonsRepository extends JpaRepository<Lesson, Long> {

    // Все занятия, отсортированные по дню и времени
    List<Lesson> findAllByOrderByDayOfWeekAscStartTimeAsc();

    @Query("SELECT l FROM Lesson l JOIN l.people p WHERE p.id = :personId")
    List<Lesson> findByPersonId(@Param("personId") Integer personId);
}
