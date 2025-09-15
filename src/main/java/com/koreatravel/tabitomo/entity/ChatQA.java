package com.koreatravel.tabitomo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "chat_qa")
@Getter
@Setter
public class ChatQA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer qaId;

    @ManyToOne
    @JoinColumn(name = "sub_category_id", referencedColumnName = "sub_category_id")
    @JsonBackReference // 순환 참조를 끊기 위해 추가
    private SubCategory subCategory;

    @Column(name = "question_ko", nullable = false)
    private String questionKo;

    @Column(name = "question_en", nullable = false)
    private String questionEn;

    @Column(name = "question_ja", nullable = false)
    private String questionJa;

    @Column(name = "answer_ko", nullable = false, columnDefinition = "TEXT")
    private String answerKo;

    @Column(name = "answer_en", nullable = false, columnDefinition = "TEXT")
    private String answerEn;

    @Column(name = "answer_ja", nullable = false, columnDefinition = "TEXT")
    private String answerJa;
}
