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
    
    @Id
    @Column(name = "info_high_num", nullable = false)
    private Integer infoHighNum;
    
    @Id
    @Column(name = "info_low_num", nullable = false)
    private Integer infoLowNum;
    
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
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
    // AddInfoEntity의 이름 가져오기
    public String getInfoName() {
        return this.addInfo != null ? this.addInfo.getInfoName() : null;
    }
    
    public String getContent() {
        return this.addInfo != null ? this.addInfo.getContent() : null;
    }
    
    // AddInfoEntity의 정보 업데이트
    public void updateAddInfo(AddInfoEntity addInfo) {
        if (addInfo != null) {
            this.infoHighNum = addInfo.getInfoHighNum();
            this.infoLowNum = addInfo.getInfoLowNum();
            this.addInfo = addInfo;
        }
    }
}
