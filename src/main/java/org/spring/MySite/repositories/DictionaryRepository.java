package org.spring.MySite.repositories;

import org.spring.MySite.models.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictionaryRepository extends JpaRepository<Dictionary, String> {

}
