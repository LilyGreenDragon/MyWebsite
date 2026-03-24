package org.spring.MySite.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson  implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message="Teacher's name should not be empty")
    @Size(min=2, max=70, message = "Teacher's name should be between 2 and 70 characters")
    @Column(name = "teacher_name", nullable = false)
    private String teacherName;

    @NotBlank(message="Lesson's name should not be empty")
    @Size(min=2, max=70, message = "Lesson's name should be between 2 and 70 characters")
    @Column(name = "lesson_name", nullable = false)
    private String lessonName;

    @NotBlank(message="Room's name should not be empty")
    @Size(min=2, max=70, message = "Room's name should be between 2 and 70 characters")
    @Column(name = "room_name", nullable = false)
    private String roomName;

    @NotNull(message="Day of week should not be empty")
    @Min(value = 1, message = "Day of week must be between 1 and 7")
    @Max(value = 7, message = "Day of week must be between 1 and 7")
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;  // 1=пн, 2=вт, 3=ср, 4=чт, 5=пт, 6=сб, 7=вс

    @NotNull(message="Start time should not be empty")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message="End time should not be empty")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @ManyToMany( mappedBy = "lessons")
    @JsonIgnore // для эндпоинта "/allmylessons", чтобы не получался  бесконечный цикл при сериализации в JSON
    private List<Person> people= new ArrayList<>();


    @Override
    public String toString() {
        return "Lesson{" +
                "id=" + id +
                ", teacherName='" + teacherName + '\'' +
                ", lessonName='" + lessonName + '\'' +
                ", roomName='" + roomName + '\'' +
                ", dayOfWeek=" + dayOfWeek +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
