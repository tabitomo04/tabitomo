package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.entity.Synonym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SynonymRepository extends JpaRepository<Synonym, Integer> {
    Optional<Synonym> findBySynonymKeyword(String synonymKeyword);
}
