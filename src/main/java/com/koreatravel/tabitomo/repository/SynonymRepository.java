package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.entity.Synonym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SynonymRepository extends JpaRepository<Synonym, Integer> {
    // JPQL을 사용하여 입력 문자열에 동의어 키워드가 포함된 경우를 찾습니다.
    @Query(value = "SELECT s FROM Synonym s WHERE :input LIKE CONCAT('%', s.synonymKeywordKo, '%') OR :input LIKE CONCAT('%', s.synonymKeywordEn, '%') OR :input LIKE CONCAT('%', s.synonymKeywordJa, '%')")
    Optional<Synonym> findBySynonymKeyword(@Param("input") String input);
}
