package com.koreatravel.tabitomo.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ChatKeyword")
@Getter // 이 어노테이션이 getQaId()와 getWeight()를 자동으로 생성합니다.
@Setter
public class ChatKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Keyword_id")
    private Integer keywordId;

    @Column(name = "qa_id")
    private Integer qaId; // getQaId()가 이 필드에 대해 생성됩니다.

    @Column(name = "keyword", length = 100)
    private String keyword;

    @Column(name = "weight")
    private Integer weight; // getWeight()가 이 필드에 대해 생성됩니다.

    // ChatQA와의 관계를 위한 ManyToOne 매핑 추가
    @ManyToOne
    @JoinColumn(name = "qa_id", insertable = false, updatable = false)
    private ChatQA chatQA;
}