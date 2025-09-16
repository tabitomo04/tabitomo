package com.koreatravel.tabitomo.entity.chat;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chat_keyword")
@Getter // 이 어노테이션이 getQaId()와 getWeight()를 자동으로 생성합니다.
@Setter
public class ChatKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Integer keywordId;

    @Column(name = "qa_id")
    private Integer qaId;

    @Column(name = "keyword_ko", length = 100, nullable = false)
    private String keywordKo;

    @Column(name = "keyword_en", length = 100, nullable = false)
    private String keywordEn;

    @Column(name = "keyword_ja", length = 100, nullable = false)
    private String keywordJa;

    @Column(name = "weight")
    private Integer weight;

    @ManyToOne
    @JoinColumn(name = "qa_id", insertable = false, updatable = false)
    @JsonBackReference
    private ChatQA chatQA;
}