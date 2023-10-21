package org.spring.MySite.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;


import java.util.*;

@Entity
@Table(name="person")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @Column(name = "name")
    private String name;


    @Column(name = "surname")
    private String surname;

    @NotEmpty(message = "Username should not be empty")
    @Size(min=2, max=30, message = "Username should be between 2 and 30 characters")
    @Column(name = "username")
    private String username;

    @NotEmpty(message = "Password should not be empty")
    //@Size(min=4, max=10, message = "Password should be between 4 and 10 characters")
    //@Length (min = 6, message = "Длина пароля не может быть менее 6 цифр")
    @Column(name = "password")
    private String password;

    @NotEmpty(message = "Email should not be empty")
    @Email(message = "Email should be valid")
    @Column(name = "email")
    private String email;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birthdate")
    private Date birthdate;

    @Column(name = "photo")
    private String photo;

    @Column(name = "image_theme")
    private String imageTheme;

    @Transient
    private String message;

    @ManyToMany(fetch= FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinTable(name= "person_roles", joinColumns = @JoinColumn(name="user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name= "role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();

    public Person(int id, String nickname, String password, String email) {
        this.id = id;
        this.name = nickname;
        this.password = password;
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;

        if (!getUsername().equals(person.getUsername())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((getUsername() == null) ? 0 : getUsername().hashCode());
        return result;
    }
}
