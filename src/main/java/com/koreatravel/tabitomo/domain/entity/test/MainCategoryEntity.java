package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "main_category")
public class MainCategoryEntity {
    @Id
    @Column(name = "main_category_id")
    private Integer mainCategoryId;
    
    @Column(name = "name_ko", nullable = false, length = 50)
    private String nameKo;
    
    @Column(name = "name_en", nullable = false, length = 50)
    private String nameEn;
    
    @Column(name = "name_ja", nullable = false, length = 50)
    private String nameJa;
    
    @Column(length = 1000)
    private String description;
    
    @OneToMany(mappedBy = "mainCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubCategoryEntity> subCategories = new ArrayList<>();
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "icon_url")
    private String iconUrl;
    
    public void addSubCategory(SubCategoryEntity subCategory) {
        subCategories.add(subCategory);
        subCategory.setMainCategory(this);
    }
    
    public void removeSubCategory(SubCategoryEntity subCategory) {
        subCategories.remove(subCategory);
        subCategory.setMainCategory(null);
    }
}
