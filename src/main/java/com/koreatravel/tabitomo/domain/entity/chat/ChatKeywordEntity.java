package com.koreatravel.tabitomo.domain.entity.chat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chat_keyword")
@Getter // 이 어노테이션이 getQaId()와 getWeight()를 자동으로 생성합니다.
@Setter
public class ChatKeywordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id", columnDefinition = "INT NOT NULL AUTO_INCREMENT")
    private Integer keywordId;

    @Column(name = "qa_id", nullable = false, columnDefinition = "INT")
    private Integer qaId;

    @Column(name = "keyword", length = 100, nullable = false)
    private String keyword;

    @Column(name = "weight", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer weight = 1;

    // ChatQA와의 관계를 위한 ManyToOne 매핑 추가
    @ManyToOne
    @JoinColumn(name = "qa_id", insertable = false, updatable = false)
    private ChatQAEntity chatQA;
}