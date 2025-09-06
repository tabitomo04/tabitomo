package com.koreatravel.tabitomo.domain.entity.chat;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Synonym")
@Data
public class SynonymEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "synonym_id")
    private Integer synonymId;

    @Column(name = "main_keyword", nullable = false, length = 100)
    private String mainKeyword;

    @Column(name = "synonym_keyword", nullable = false, length = 100)
    private String synonymKeyword;
}
