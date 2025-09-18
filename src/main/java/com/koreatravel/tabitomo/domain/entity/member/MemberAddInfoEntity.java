package com.koreatravel.tabitomo.domain.entity.member;

import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

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
    @Column(name = "member_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID memberId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(
            name = "info_high_num",
            referencedColumnName = "info_high_num",
            insertable = false,
            updatable = false
        ),
        @JoinColumn(
            name = "info_low_num",
            referencedColumnName = "info_low_num",
            insertable = false,
            updatable = false
        )
    })
    private AddInfoEntity addInfo;
    
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
