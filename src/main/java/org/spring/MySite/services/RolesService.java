package org.spring.MySite.services;


import org.spring.MySite.models.Role;
import org.spring.MySite.repositories.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RolesService {

    @Autowired
    private RolesRepository rolesRepository;

    public Optional<Role> findByName(String name) {
        return rolesRepository.findByName(name);
    }

}
