package com.koreatravel.tabitomo.domain.entity.trip;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "trip")
@Builder
@AllArgsConstructor
public class TripEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long id;

    @Column(name = "member_email", nullable = false, length = 320)
    private String memberEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_email", referencedColumnName = "email", insertable = false, updatable = false)
    private MemberEntity member;

    @Column(name = "accommodation_id", length = 50)
    private String accommodationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", insertable = false, updatable = false)
    private PlaceEntity accommodation;

    @Column(nullable = false, length = 255)
    private String title;
    
    public void setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
    }

    @Column(name = "start_date")
    private LocalDate startDate;
    
    public void setStartDate(LocalDate startDate) {
        if (startDate != null) {
            this.startDate = startDate;
        }
    }

    @Column(name = "end_date")
    private LocalDate endDate;
    
    public void setEndDate(LocalDate endDate) {
        if (endDate != null) {
            this.endDate = endDate;
        }
    }

    @Column(length = 20)
    private String visibility; // e.g., "PUBLIC", "PRIVATE"

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScheduleEntity> schedules = new ArrayList<>();

    // Helper methods
    public void addSchedule(ScheduleEntity schedule) {
        this.schedules.add(schedule);
        schedule.setTrip(this);
    }

    public void removeSchedule(ScheduleEntity schedule) {
        this.schedules.remove(schedule);
        schedule.setTrip(null);
    }

    public void setAccommodation(PlaceEntity accommodation) {
        this.accommodation = accommodation;
        this.accommodationId = accommodation != null ? accommodation.getId() : null;
    }
    
    public void updateAccommodation(PlaceEntity accommodation) {
        setAccommodation(accommodation);
    }
    
    // Update accommodation by ID
    public void updateAccommodationId(String accommodationId) {
        this.accommodationId = accommodationId;
        this.accommodation = null; // Will be loaded on demand
    }

    public void updateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateVisibility(String visibility) {
        if (visibility != null && (visibility.equals("PUBLIC") || visibility.equals("PRIVATE"))) {
            this.visibility = visibility;
        }
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalStateException("Start date must be before or equal to end date");
        }
        if (visibility == null) {
            visibility = "PRIVATE";
        }
    }
}
