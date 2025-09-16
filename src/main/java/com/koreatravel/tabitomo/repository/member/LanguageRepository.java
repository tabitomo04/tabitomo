package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanguageRepository extends JpaRepository<LanguageEntity, Integer> {
    @Query("SELECT l FROM LanguageEntity l ORDER BY l.nameEn ASC")
    List<LanguageEntity> findAllActiveLanguages();

    LanguageEntity findById(int id);
}