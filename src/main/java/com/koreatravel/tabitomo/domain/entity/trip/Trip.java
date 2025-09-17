package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.tag.TagMasterEntity;
import com.koreatravel.tabitomo.domain.entity.tag.TripTagEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "trip_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id")
    private Place accommodation;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Schedule> schedules = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 20)
    private String visibility; // e.g., "PUBLIC", "PRIVATE"

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TripTagEntity> tags = new ArrayList<>();
    
    // Helper method to add a tag
    public void addTag(TagMasterEntity tag) {
        TripTagEntity tripTag = new TripTagEntity();
        tripTag.setTrip(this);
        tripTag.setTag(tag);
        tags.add(tripTag);
    }
    
    // Helper method to remove a tag
    public void removeTag(TagMasterEntity tag) {
        tags.removeIf(tripTag -> tripTag.getTag().equals(tag));
    }

    @Builder
    public Trip(MemberEntity member, Place accommodation, String title, LocalDate startDate, LocalDate endDate, String visibility) {
        this.member = member;
        this.accommodation = accommodation;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.visibility = visibility;
    }

    //== Business Methods ==//
    /**
     * 여행 제목을 수정합니다.
     */
    public void updateTitle(String title) {
        this.title = title;
    }
}
