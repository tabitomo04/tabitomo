package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "language")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT")
    private Integer languageId;
    
    @Column(name = "language_code", length = 5, unique = true, nullable = false)
    private String languageCode;  // ISO 639-1 (e.g., ko, en, ja, zh)
    
    @Column(name = "name_native", nullable = false, length = 50)
    private String nameNative;  // 원어명 (e.g., 한국어, English, 日本語)
    
    @Column(name = "name_en", nullable = false, length = 50)
    private String nameEn;  // 영어명 (e.g., Korean, English, Japanese)
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "preferredLanguage", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MemberEntity> members = new ArrayList<>();
    
    // For DTO conversion
    public Integer getLanguageId() {
        return languageId;
    }
    
    public String getNameNative() {
        return nameNative;
    }
    
    public String getNameEn() {
        return nameEn;
    }
    
    // 생성 메서드
    public static LanguageEntity createLanguage(String languageCode, String nameNative, String nameEn) {
        LanguageEntity language = new LanguageEntity();
        language.setLanguageCode(languageCode);
        language.setNameNative(nameNative);
        language.setNameEn(nameEn);
        return language;
    }
}
