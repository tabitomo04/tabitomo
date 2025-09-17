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
    
    @Column(name = "country_name", nullable = false, length = 100)
    private String countryName;  // 국가명
    
    @Column(name = "region", length = 50)
    private String region;
    
    @Column(name = "iso_code", length = 3)
    private String isoCode;  // ISO 3166-1 alpha-3 (e.g., KOR, USA, JPN)
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MemberEntity> members = new ArrayList<>();
    
    // For DTO conversion
    public Integer getCountryId() {
        return countryId;
    }
    
    public String getCountryName() {
        return countryName;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public String getIsoCode() {
        return isoCode;
    }
    
    public String getRegion() {
        return region;
    }
    
    // 생성 메서드
    public static CountryEntity createCountry(String countryCode, String countryName, String region, String isoCode) {
        CountryEntity country = new CountryEntity();
        country.setCountryCode(countryCode);
        country.setCountryName(countryName);
        country.setRegion(region);
        country.setIsoCode(isoCode);
        return country;
    }
}
