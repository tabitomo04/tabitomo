package com.koreatravel.tabitomo.domain.entity.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.Column;

@Data
@Entity
@Table(name = "chat_category")
public class ChatCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", columnDefinition = "INT NOT NULL AUTO_INCREMENT")
    private Integer categoryId;
    @Column(name = "name", length = 100, nullable = false)
    private String name;
}
