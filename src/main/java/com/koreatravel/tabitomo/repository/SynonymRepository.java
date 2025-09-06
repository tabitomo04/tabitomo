package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.domain.entity.SynonymEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SynonymRepository extends JpaRepository<SynonymEntity, Integer> {

    // Finds a Synonym entity by its synonym_keyword.
    // We use Optional to handle cases where no matching synonym is found.
    Optional<SynonymEntity> findBySynonymKeyword(String synonymKeyword);
}
