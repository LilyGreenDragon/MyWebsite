package org.spring.MySite.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.spring.MySite.util.MinimumDate;
import org.springframework.format.annotation.DateTimeFormat;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name="person")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Size(min=0, max=50, message = "Name should be less than 50 characters")
    @Column(name = "name")
    private String name;

    @Size(min=0, max=50, message = "Surname should be less then 50 characters")
    @Column(name = "surname")
    private String surname;

   // @NotEmpty(message = "Username should not be empty")
    @Size(min=2, max=30, message = "Username should be between 2 and 30 characters")
    @Column(name = "username")
    private String username;

    //@NotEmpty(message = "Password should not be empty")
    @JsonIgnore
    //@Size(min=4, max=10, message = "Password should be between 4 and 10 characters")
    //@Length (min = 6, message = "Длина пароля не может быть менее 6 цифр")
    @Column(name = "password")
    private String password;

   // @NotEmpty(message = "Email should not be empty")
    @Size(min=0, max=70, message = "Email should be less then 70 characters")
    @Email(message = "Email should be valid")
    @Column(name = "email")
    private String email;

    @MinimumDate
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    //@JsonFormat(pattern = "dd.MM.yyyy")
    //@DateTimeFormat(pattern = "dd.MM.yyyy")
    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "photo")
    private String photo;

    @Column(name = "image_theme")
    private String imageTheme;

    @Transient
    @Size(min=0, max=200, message = "Message should be less than 200 characters")
    private String message;

    @ManyToMany(fetch= FetchType.EAGER, cascade = {CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinTable(name= "person_roles", joinColumns = @JoinColumn(name="person_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name= "role_id", referencedColumnName = "id"))
    @JsonIgnore
    private List<Role> roles = new ArrayList<>();

    public Person(int id,String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public Person(String name, String surname, LocalDate birthdate) {
        this.name = name;
        this.surname = surname;
        this.birthdate = birthdate;
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
