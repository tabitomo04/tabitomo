package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "favorite_place")
public class FavoritePlaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;
    
    @Column(name = "place_id", nullable = false, length = 50)
    private String placeId;
    
    @Column(name = "place_name", length = 200)
    private String placeName;
    
    @Column(length = 500)
    private String address;
    
    private Double latitude;
    private Double longitude;
    
    @Column(length = 50)
    private String category;
    
    @Column(length = 1000)
    private String memo;
    
    @Column(name = "visit_date")
    private LocalDateTime visitDate;
    
    @Column(name = "is_public")
    private Boolean isPublic = false;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
