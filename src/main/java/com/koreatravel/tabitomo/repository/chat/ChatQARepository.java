package com.koreatravel.tabitomo.repository.chat;

import com.koreatravel.tabitomo.entity.ChatQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatQARepository extends JpaRepository<ChatQA, Integer> {
    List<ChatQA> findBySubCategoryId(Integer subCategoryId);
}