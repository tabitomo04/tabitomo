package com.koreatravel.tabitomo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Synonym")
@Getter
@Setter
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
