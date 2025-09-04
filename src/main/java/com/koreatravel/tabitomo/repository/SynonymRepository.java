package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.entity.Synonym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SynonymRepository extends JpaRepository<Synonym, Integer> {

    // Finds a Synonym entity by its synonym_keyword.
    // We use Optional to handle cases where no matching synonym is found.
    Optional<Synonym> findBySynonymKeyword(String synonymKeyword);
}
