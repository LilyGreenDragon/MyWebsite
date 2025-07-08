package org.spring.MySite.security;

import org.spring.MySite.models.Person;
import org.spring.MySite.models.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

public class PersonDetails implements UserDetails {

    private final Person person;

    public PersonDetails(Person person) {
        this.person = person;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return person.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

   /* @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<Role> roles = person.getRoles();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return authorities;
    }*/
   //Если все юзеры
    /*@Override
   public Collection<? extends GrantedAuthority> getAuthorities() {
       return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
   }*/

    @Override
    public String getPassword() {  return this.person.getPassword();  }

    @Override
    public String getUsername() {
        return this.person.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //есть метод isAccountNonLocked()

    @Override
    public boolean isAccountNonLocked() {
        /*Collection<? extends GrantedAuthority> authorities = mapRolesToAuthorities(person.getRoles());
        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals("BLOCKED")) {
                return false;
            }
        }*/

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Нужно, чтобы получать данные аутентифицированного пользователя
    public Person getPerson() {
        return this.person;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonDetails that = (PersonDetails) o;
        return person.equals(that.person);
    }

    @Override
    public int hashCode() {
        //return Objects.hash(person);
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((getPerson() == null) ? 0 : getPerson().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "PersonDetails{" +
                "person=" + person +
                '}';
    }


}