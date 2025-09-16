package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "trip")
public class TripEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long tripId;
    
    @Column(name = "member_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID memberId;
    
    @Column(name = "accommodation_id", length = 50)
    private String accommodationId;
    
    @Column(name = "title", length = 200)
    private String title;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "destination", length = 100)
    private String destination;
    
    @Column(name = "status", length = 20)
    private String status;
    
    @Column(name = "is_public", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isPublic = false;
    
    @Column(name = "view_count", columnDefinition = "INT DEFAULT 0")
    private Integer viewCount = 0;
    
    @Column(name = "like_count", columnDefinition = "INT DEFAULT 0")
    private Integer likeCount = 0;
    
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
