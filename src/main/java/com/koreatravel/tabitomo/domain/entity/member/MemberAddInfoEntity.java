package com.koreatravel.tabitomo.domain.entity.member;

import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member_add_info")
@IdClass(MemberAddInfoId.class)
public class MemberAddInfoEntity {
    
    @Id
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Id
    @Column(name = "info_high_num", nullable = false)
    private Integer infoHighNum;
    
    @Id
    @Column(name = "info_low_num", nullable = false)
    private Integer infoLowNum;
    
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
