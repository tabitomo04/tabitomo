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

    @Column(name = "synonym_keyword", nullable = false)
    private String synonymKeyword;

    public ChatKeyword getChatKeyword() {
        return this.chatKeyword;
    }
}
