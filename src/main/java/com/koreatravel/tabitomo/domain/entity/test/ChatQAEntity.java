package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "chat_qa")
public class ChatQAEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qa_id")
    private Long qaId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategoryEntity subCategory;
    
    @Column(name = "question_ko", nullable = false, length = 255)
    private String questionKo;
    
    @Column(name = "question_en", nullable = false, length = 255)
    private String questionEn;
    
    @Column(name = "question_ja", nullable = false, length = 255)
    private String questionJa;
    
    @Column(name = "answer_ko", nullable = false, columnDefinition = "TEXT")
    private String answerKo;
    
    @Column(name = "answer_en", nullable = false, columnDefinition = "TEXT")
    private String answerEn;
    
    @Column(name = "answer_ja", nullable = false, columnDefinition = "TEXT")
    private String answerJa;
    
    @OneToMany(mappedBy = "chatQA", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatKeywordEntity> keywords = new ArrayList<>();
    
    public void addKeyword(ChatKeywordEntity keyword) {
        keywords.add(keyword);
        keyword.setChatQA(this);
    }
    
    public void removeKeyword(ChatKeywordEntity keyword) {
        keywords.remove(keyword);
        keyword.setChatQA(null);
    }
}
