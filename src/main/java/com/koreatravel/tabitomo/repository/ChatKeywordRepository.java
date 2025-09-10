package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.entity.ChatKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatKeywordRepository extends JpaRepository<ChatKeyword, Integer> {

    // 사용자의 입력 문자열에 포함된 모든 키워드를 찾는 쿼리
    @Query("SELECT ck FROM ChatKeyword ck WHERE :input LIKE CONCAT('%', ck.keyword, '%')")
    List<ChatKeyword> findByKeywordInInput(@Param("input") String input);
}