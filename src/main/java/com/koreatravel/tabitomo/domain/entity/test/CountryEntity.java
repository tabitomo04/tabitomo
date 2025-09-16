package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "country")
public class CountryEntity {
    @Id
    @Column(name = "country_code", length = 2)
    private String countryCode;
    
    @Column(name = "country_name_ko", nullable = false, length = 100)
    private String countryNameKo;
    
    @Column(name = "country_name_en", nullable = false, length = 100)
    private String countryNameEn;
    
    @Column(name = "phone_code", length = 10)
    private String phoneCode;
    
    @Column(name = "flag_image_url")
    private String flagImageUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "time_zone", length = 50)
    private String timeZone;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
