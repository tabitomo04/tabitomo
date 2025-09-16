package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "language")
public class LanguageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "language_code", nullable = false, unique = true, length = 10)
    private String languageCode;
    
    @Column(name = "name_ko", nullable = false, length = 50)
    private String nameKo;
    
    @Column(name = "name_en", nullable = false, length = 50)
    private String nameEn;
    
    @Column(name = "name_ja", nullable = false, length = 50)
    private String nameJa;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
