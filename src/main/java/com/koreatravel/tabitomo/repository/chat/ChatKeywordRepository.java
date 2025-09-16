package com.koreatravel.tabitomo.repository.chat;

import com.koreatravel.tabitomo.entity.ChatKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatKeywordRepository extends JpaRepository<ChatKeyword, Integer> {

    // JPQL을 사용하여 입력 문자열에 키워드가 포함된 경우를 찾습니다.
    // 기존의 findByKeywordInInput 메서드 대신 이 메서드를 사용합니다.
    @Query(value = "SELECT ck FROM ChatKeyword ck WHERE :input LIKE CONCAT('%', ck.keywordKo, '%') OR :input LIKE CONCAT('%', ck.keywordEn, '%') OR :input LIKE CONCAT('%', ck.keywordJa, '%')")
    List<ChatKeyword> findByKeywordInInput(@Param("input") String input);
}