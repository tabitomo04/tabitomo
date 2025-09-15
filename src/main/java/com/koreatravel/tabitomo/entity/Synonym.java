package com.koreatravel.tabitomo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "synonym")
@Getter
@Setter
public class Synonym {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer synonymId;

    @ManyToOne
    @JoinColumn(name = "keyword_id", nullable = false)
    private ChatKeyword chatKeyword;

    @Column(name = "synonym_keyword_ko", nullable = false)
    private String synonymKeywordKo;

    @Column(name = "synonym_keyword_en", nullable = false)
    private String synonymKeywordEn;

    @Column(name = "synonym_keyword_ja", nullable = false)
    private String synonymKeywordJa;

    public ChatKeyword getChatKeyword() {
        return this.chatKeyword;
    }
}
