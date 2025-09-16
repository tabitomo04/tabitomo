package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "chat_keyword")
public class ChatKeywordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Long keywordId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qa_id", nullable = false)
    private ChatQAEntity chatQA;
    
    @Column(name = "keyword_ko", nullable = false, length = 100)
    private String keywordKo;
    
    @Column(name = "keyword_en", nullable = false, length = 100)
    private String keywordEn;
    
    @Column(name = "keyword_ja", nullable = false, length = 100)
    private String keywordJa;
    
    @Column(nullable = false)
    private Integer weight = 1;
}
