package org.spring.MySite.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor

public class Dinner {
    @NotNull
    //@Size(min=4, message="Name must be at least 4 characters long")
    private String name;

    //@Size(min=1, message="You must choose at least 1 ingredient")
    private List<Element> elementsDinner;
}
