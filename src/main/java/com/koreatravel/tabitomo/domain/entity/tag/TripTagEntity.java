package com.koreatravel.tabitomo.domain.entity.tag;

import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trip_tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TripTagId.class)
public class TripTagEntity {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false, insertable = false, updatable = false)
    private Trip trip;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false, insertable = false, updatable = false)
    private TagMasterEntity tag;
    
    // Composite key fields (must match the @IdClass fields)
    @Column(name = "trip_id")
    private Long tripId;
    
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
