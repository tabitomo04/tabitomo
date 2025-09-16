package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "sub_category")
public class SubCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_category_id")
    private Integer subCategoryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_category_id", nullable = false)
    private MainCategoryEntity mainCategory;
    
    @Column(name = "name_ko", nullable = false, length = 50)
    private String nameKo;
    
    @Column(name = "name_en", nullable = false, length = 50)
    private String nameEn;
    
    @Column(name = "name_ja", nullable = false, length = 50)
    private String nameJa;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatQAEntity> chatQAs = new ArrayList<>();
    
    public void addChatQA(ChatQAEntity chatQA) {
        chatQAs.add(chatQA);
        chatQA.setSubCategory(this);
    }
    
    public void removeChatQA(ChatQAEntity chatQA) {
        chatQAs.remove(chatQA);
        chatQA.setSubCategory(null);
    }
}
