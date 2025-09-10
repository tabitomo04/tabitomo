package com.koreatravel.tabitomo.dto.trip;

import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlaceEntity;
import com.koreatravel.tabitomo.domain.entity.trip.PlaceEntity;
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

    public static FavoritePlaceDetailDTO fromEntity(FavoritePlaceEntity favorite, PlaceEntity place) {
        return FavoritePlaceDetailDTO.builder()
                .id(favorite.getId())
                .placeId(favorite.getPlaceId())
                .name(place != null ? place.getName() : null)
                .address(place != null ? place.getAddress() : null)
                .categoryCode(place != null ? place.getCategoryCode() : null)
                .imageUrl(place != null ? place.getImageUrl() : null)
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
