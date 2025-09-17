package com.koreatravel.tabitomo.domain.entity.tag;

import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class TripTagId implements Serializable {
    @Column(name = "trip_id")
    private Long tripId;
    
    @Column(name = "tag_id")
    private Integer tagId;
}

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
    private TagMaster tag;
    
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
    public void setTag(TagMaster tag) {
        this.tag = tag;
        this.tagId = tag != null ? tag.getTagId() : null;
    }
}
