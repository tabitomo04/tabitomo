package com.koreatravel.tabitomo.domain.entity.chat;

import jakarta.persistence.*;

@Entity
@Table(name = "forbidden_word")
public class ForbiddenWordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_id", columnDefinition = "INT NOT NULL AUTO_INCREMENT")
    private Integer wordId;

    @Column(name = "word", nullable = false, unique = true, length = 100)
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
