package com.koreatravel.tabitomo.entity.chat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "sub_category")
@Getter
@Setter
public class SubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_category_id")
    private Integer id;

    @Column(name = "name_ko", nullable = false)
    private String nameKo;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_ja", nullable = false)
    private String nameJa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_category_id")
    @JsonBackReference
    private MainCategory mainCategory;

    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ChatQA> chatQAs;
}
