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

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;
}
