package com.koreatravel.tabitomo.domain.dto.trip;

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
public class FavoritePlaceDetailDTO {
    private Integer id;
    private String placeId;
    private String name;
    private String address;
    private String categoryCode;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static FavoritePlaceDetailDTO fromEntity(FavoritePlaceEntity favorite) {
        return FavoritePlaceDetailDTO.builder()
                .id(favorite.getId())
                .placeId(favorite.getPlaceId())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
