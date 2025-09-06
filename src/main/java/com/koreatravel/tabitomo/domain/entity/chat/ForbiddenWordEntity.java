package com.koreatravel.tabitomo.domain.entity.chat;

import jakarta.persistence.*;

@Entity
@Table(name = "ForbiddenWord")
public class ForbiddenWordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer wordId;

    @Column(nullable = false, unique = true)
    private String word;

    public Integer getWordId() {
        return wordId;
    }

    public void setWordId(Integer wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
