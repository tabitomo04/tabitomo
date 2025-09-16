package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "synonym")
public class SynonymEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "synonym_id")
    private Long synonymId;
    
    @Column(name = "keyword_id", nullable = false)
    private Integer keywordId;
    
    @Column(name = "synonym_keyword_ko", nullable = false, length = 100, columnDefinition = "VARCHAR(100) COMMENT '한국어 동의어'")
    private String synonymKeywordKo;
    
    @Column(name = "synonym_keyword_en", length = 100, columnDefinition = "VARCHAR(100) COMMENT '영어 동의어'")
    private String synonymKeywordEn;
    
    @Column(name = "synonym_keyword_ja", length = 100, columnDefinition = "VARCHAR(100) COMMENT '일본어 동의어'")
    private String synonymKeywordJa;
    
    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
