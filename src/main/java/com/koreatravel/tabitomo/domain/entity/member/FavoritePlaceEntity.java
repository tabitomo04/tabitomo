package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자가 즐겨찾기한 장소를 나타내는 엔티티
 */
@Entity
@Table(name = "favorite_place")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritePlaceEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_email", nullable = false)
    private MemberEntity member;
    
    @Column(name = "place_id", nullable = false, length = 100)
    private String placeId;
    
    @Column(name = "place_name", nullable = false, length = 100)
    private String placeName;
    
    @Column(name = "address", length = 200)
    private String address;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
