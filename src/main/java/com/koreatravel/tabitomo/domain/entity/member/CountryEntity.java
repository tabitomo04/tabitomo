package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "country")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id", columnDefinition = "INT")
    private Integer countryId;
    
    @Column(name = "country_code", length = 2, unique = true, nullable = false)
    private String countryCode;  // ISO 3166-1 alpha-2 (e.g., KR, US, JP)
    
    @Column(name = "name_kr", nullable = false, length = 50)
    private String nameKr;  // 한국어 국가명
    
    @Column(name = "name_en", nullable = false, length = 50)
    private String nameEn;  // 영어 국가명
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MemberEntity> members = new ArrayList<>();
    
    // Getters for the new fields
    public String getNameKr() {
        return nameKr;
    }

    public String getNameEn() {
        return nameEn;
    }
    
    // For DTO conversion
    public Integer getCountryId() {
        return countryId;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    // 생성 메서드
    public static CountryEntity createCountry(String countryCode, String nameKr, String nameEn) {
        return CountryEntity.builder()
                .countryCode(countryCode)
                .nameKr(nameKr)
                .nameEn(nameEn)
                .build();
    }
}
