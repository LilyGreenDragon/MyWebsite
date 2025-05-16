package org.spring.MySite.models;


import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name="roles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {
        @Id
        @Column(name="id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @Column(name = "name")
        private String name;


        /*@ManyToMany(fetch= FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name= "Person_Roles", joinColumns = @JoinColumn(name="role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name= "user_id", referencedColumnName = "id"))*/

    @ManyToMany( mappedBy = "roles")
    //@JsonIgnore
    private List<Person> people= new ArrayList<>();


    public Role(String name) {
        this.name = name;

    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +

                '}';
    }
}
