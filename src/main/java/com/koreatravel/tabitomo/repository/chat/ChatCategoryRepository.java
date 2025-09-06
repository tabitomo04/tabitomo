package com.koreatravel.tabitomo.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import com.koreatravel.tabitomo.domain.entity.chat.ChatCategoryEntity;

public interface ChatCategoryRepository extends JpaRepository<ChatCategoryEntity, Integer> {}
