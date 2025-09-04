package com.koreatravel.tabitomo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data // @Data 어노테이션 추가
@Entity
public class ChatQA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qa_id")
    private Integer qaId;

    @Column(name = "category_id")
    private Integer categoryId;

    private String question;

    private String answer;
}
