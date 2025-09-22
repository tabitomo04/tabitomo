package com.koreatravel.tabitomo.domain.entity.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripTagId implements Serializable {
    @Column(name = "trip_id")
    private Long tripId;
    
    @Column(name = "tag_id")
    private Integer tagId;
}
