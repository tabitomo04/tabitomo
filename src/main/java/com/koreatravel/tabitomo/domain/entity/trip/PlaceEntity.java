package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "place")
public class PlaceEntity {

    @Id
    @Column(name = "place_id", nullable = false, length = 50)
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "category_code", length = 50)
    private String categoryCode; // 예: ATTRACTION, RESTAURANT, ACCOMMODATION

    @Column(name = "price_range", length = 50)
    private String priceRange; // 가격대 정보 (예: "10000-20000")

    @Column(length = 200)
    private String address;

    private Double latitude;
    private Double longitude;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "place", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScheduleEntity> schedules = new ArrayList<>();

    // Helper method to add a schedule
    public void addSchedule(ScheduleEntity schedule) {
        this.schedules.add(schedule);
        schedule.setPlace(this);
    }

    // Helper method to remove a schedule
    public void removeSchedule(ScheduleEntity schedule) {
        this.schedules.remove(schedule);
        schedule.setPlace(null);
    }
    
    // Get the place ID (for convenience)
    public String getPlaceId() {
        return this.id;
    }
}
