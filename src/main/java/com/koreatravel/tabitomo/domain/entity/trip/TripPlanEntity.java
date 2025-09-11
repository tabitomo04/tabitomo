package com.koreatravel.tabitomo.domain.entity.trip;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "trip_plan")
public class TripPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_plan_id")
    private Long id;

    @Column(name = "member_email", nullable = false, length = 320)
    private String memberEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_email", referencedColumnName = "email", insertable = false, updatable = false)
    private MemberEntity member;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Lob
    @Column(name = "plan_details", nullable = false, columnDefinition = "TEXT")
    private String planDetails;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // Helper method to update plan details
    public void updatePlanDetails(String planDetails) {
        if (planDetails != null) {
            this.planDetails = planDetails;
        }
    }

    // Helper method to update plan name
    public void updatePlanName(String planName) {
        if (planName != null && !planName.trim().isEmpty()) {
            this.planName = planName.trim();
        }
    }
}
