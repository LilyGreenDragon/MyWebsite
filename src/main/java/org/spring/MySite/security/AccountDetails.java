package org.spring.MySite.security;

import org.spring.MySite.models.Person;
import org.springframework.security.core.GrantedAuthority;


import java.util.Collection;

public class AccountDetails extends org.springframework.security.core.userdetails.User {
    private final PersonDetails personDetails;

    public AccountDetails(PersonDetails personDetails,
                          Collection<? extends GrantedAuthority> authorities) {
        super(personDetails.getPerson().getEmail(), personDetails.getPassword(), authorities);
        this.personDetails= personDetails;
    }

    public PersonDetails getAccount() {
        return personDetails;
    }

    public Person getMember() {
        return personDetails.getPerson();
    }
}
