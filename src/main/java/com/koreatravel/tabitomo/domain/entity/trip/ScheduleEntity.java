package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "schedule")
@Builder
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @Column(name = "place_id", nullable = false, length = 50)
    private String placeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", insertable = false, updatable = false)
    private PlaceEntity place;

    @Column(nullable = false)
    private int day; // 1일차, 2일차...

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "end_time")
    private String endTime;

    @Lob
    private String memo;

    @Builder
    public ScheduleEntity(TripEntity trip, PlaceEntity place, String placeId, int day, String startTime, String endTime, String memo) {
        this.trip = trip;
        this.place = place;
        this.placeId = place != null ? place.getId() : placeId;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
    }
    
    public void updateMemo(String memo) {
        this.memo = memo != null ? memo : "";
    }
    
    public void setTrip(TripEntity trip) {
        this.trip = trip;
    }
    
    public void setPlace(PlaceEntity place) {
        this.place = place;
        this.placeId = place != null ? place.getId() : null;
    }
}
