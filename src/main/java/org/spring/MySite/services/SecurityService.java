package org.spring.MySite.services;

import org.spring.MySite.models.Person;
import org.spring.MySite.security.PersonDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    @Autowired
    private PeopleService peopleService;

    public Person resolvePrincipal(Object principal) {

        if (principal instanceof PersonDetails) {
            return ((PersonDetails) principal).getPerson();
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            String username = oauth2User.getAttribute("login");
            return peopleService.findByUsername(username).get();

                    //.orElseGet(() -> convertOAuth2UserToPerson(oauthUser));
        }
        return null;
    }


   /* private Person convertOAuth2UserToPerson(OAuth2User oauthUser) {
        Person person = new Person();
        person.setUsername(oauthUser.getAttribute("login"));
        person.setEmail(oauthUser.getAttribute("email"));
        peopleService.save(person)
        return person;
    }*/
}

