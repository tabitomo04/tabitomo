package com.koreatravel.tabitomo.repository;
import com.koreatravel.tabitomo.entity.ChatQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatQARepository extends JpaRepository<ChatQA, Integer> {

    // categoryId 필드를 기준으로 ChatQA 목록을 찾는 메소드
    List<ChatQA> findByCategoryId(Integer categoryId);
}