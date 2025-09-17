package com.koreatravel.tabitomo.domain.entity.tag;

import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "trip_tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TripTagId.class)
public class TripTagEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", referencedColumnName = "trip_id", insertable = false, updatable = false)
    private Trip trip;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", referencedColumnName = "tag_id", insertable = false, updatable = false)
    private TagMasterEntity tag;
    
    @Id
    @Column(name = "trip_id", columnDefinition = "BINARY(16)")
    private UUID tripId;
    
    @Id
    @Column(name = "tag_id")
    private Integer tagId;
    
    // Helper method to set both the relationship and the ID
    public void setTrip(Trip trip) {
        this.trip = trip;
        this.tripId = trip != null ? trip.getId() : null;
    }
    
    // Helper method to set both the relationship and the ID
    public void setTag(TagMasterEntity tag) {
        this.tag = tag;
        this.tagId = tag != null ? tag.getTagId() : null;
    }
}
