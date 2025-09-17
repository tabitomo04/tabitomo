package com.koreatravel.tabitomo.domain.entity.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripTagId implements Serializable {
    @Column(name = "trip_id", columnDefinition = "BINARY(16)")
    private UUID tripId;
    
    @Column(name = "tag_id")
    private Integer tagId;
}
