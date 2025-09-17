package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.domain.entity.tag.TagMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface tagMasterRepository extends JpaRepository<TagMasterEntity, Integer> {

    Optional<TagMasterEntity> findBytagName(String tagName);

    // 랜덤 태그 가져오기
    @Query(value = "SELECT tag_name FROM tagmaster ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<String> findRandomTagNames();
}
