package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "place")
public class PlaceEntity {
    
    @Id
    @Column(name = "place_id", length = 50)
    private String placeId;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "category_code", length = 50)
    private String categoryCode;
    
    @Column(name = "category_name", length = 100)
    private String categoryName;
    
    @Column(name = "address", length = 255)
    private String address;
    
    @Column(name = "road_address", length = 255)
    private String roadAddress;
    
    @Column(name = "mapx")
    private Double mapX;
    
    @Column(name = "mapy")
    private Double mapY;
    
    @Column(name = "tel", length = 50)
    private String tel;
    
    @Column(name = "homepage", length = 500)
    private String homepage;
    
    @Column(name = "overview", columnDefinition = "TEXT")
    private String overview;
    
    @Column(name = "first_image", length = 500)
    private String firstImage;
    
    @Column(name = "second_image", length = 500)
    private String secondImage;
    
    @Column(name = "sido_code")
    private Integer sidoCode;
    
    @Column(name = "sigungu_code")
    private Integer sigunguCode;
    
    @Column(name = "view_count", columnDefinition = "int default 0")
    private Integer viewCount = 0;
    
    @Column(name = "like_count", columnDefinition = "int default 0")
    private Integer likeCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
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
