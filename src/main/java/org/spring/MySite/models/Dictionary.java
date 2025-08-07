package org.spring.MySite.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="dictionary")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dictionary {

    @Id
    @Size(min=0, max=50, message = "Name should be less than 50 characters")
    @Column(name = "name")
    private String name;

    @Size(min=0, max=50, message = "Meaning should be less than 50 characters")
    @Column(name = "meaning")
    private String meaning;

}
