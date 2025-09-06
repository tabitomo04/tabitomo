package com.koreatravel.tabitomo.repository;
import com.koreatravel.tabitomo.domain.entity.ChatQAEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatQARepository extends JpaRepository<ChatQAEntity, Integer> {

    // categoryId 필드를 기준으로 ChatQA 목록을 찾는 메소드
    List<ChatQAEntity> findByCategoryId(Integer categoryId);
}