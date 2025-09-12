package com.koreatravel.tabitomo.domain.entity.member;

import com.koreatravel.tabitomo.id.MemberAddInfoId;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * MemberAddInfoEntity represents the relationship between a member and their selected additional information.
 * This entity maps to the member_add_info join table in the database.
 */
@Entity
@Table(name = "member_add_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAddInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    private MemberAddInfoId id;
    
    @Column(name = "created_at", nullable = false, updatable = false, 
            insertable = false, 
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    // Relationships
    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id", insertable = false, updatable = false)
    private MemberEntity member;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "info_high_num", referencedColumnName = "info_high_num", insertable = false, updatable = false),
        @JoinColumn(name = "info_low_num", referencedColumnName = "info_low_num", insertable = false, updatable = false)
    })
    private AddInfoEntity addInfo;
    
    // Helper methods to access composite key fields
    public UUID getMemberId() {
        return id != null ? id.getMemberId() : null;
    }

    public Integer getInfoHighNum() {
        return id != null ? id.getInfoHighNum() : null;
    }

    public Integer getInfoLowNum() {
        return id != null ? id.getInfoLowNum() : null;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        
        // Initialize the composite ID if not set
        if (this.id == null && this.member != null && this.addInfo != null) {
            this.id = new MemberAddInfoId(
                member.getId(),
                addInfo.getInfoHighNum(),
                addInfo.getInfoLowNum()
            );
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberAddInfoEntity that = (MemberAddInfoEntity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "MemberAddInfoEntity{" +
               "id=" + id +
               ", createdAt=" + (createdAt != null ? createdAt.toString() : "null") +
               '}';
    }
}
