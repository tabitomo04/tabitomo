package com.koreatravel.tabitomo.dto.trip;

import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlaceEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoritePlaceDTO {
    private Integer id;
    private String placeId;
    private LocalDateTime createdAt;

    public static FavoritePlaceDTO fromEntity(FavoritePlaceEntity entity) {
        return FavoritePlaceDTO.builder()
                .id(entity.getId())
                .placeId(entity.getPlaceId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
