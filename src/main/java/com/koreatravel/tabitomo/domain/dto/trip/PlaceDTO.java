package com.koreatravel.tabitomo.domain.dto.trip;

import com.koreatravel.tabitomo.domain.entity.trip.PlaceEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data   
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDTO {
    private String id;
    private String poi_id;
    private String poi_name;
    private String category;
    private String ctprvn;
    private String sigungu;
    private String legaldong;
    private String li_nm;
    private String lnbr_no;
    private String buld_no;
    private String lc_lo;
    private String lc_la;

    private boolean isFavorite;

    public static PlaceDTO toPlaceDTO(PlaceEntity place) {
        return PlaceDTO.builder()
                .id(place.getId())
                .poi_id(place.getPoi_id())
                .poi_name(place.getPoi_name())
                .category(place.getCategory())
                .ctprvn(place.getCtprvn())
                .sigungu(place.getSigungu())
                .legaldong(place.getLegaldong())
                .li_nm(place.getLi_nm())
                .lnbr_no(place.getLnbr_no())
                .buld_no(place.getBuld_no())
                .lc_lo(place.getLc_lo())
                .lc_la(place.getLc_la())
                .build();
    }

    public static PlaceEntity toPlaceEntity(PlaceDTO place) {
        return PlaceEntity.builder()
                .id(place.getId())
                .poi_id(place.getPoi_id())
                .poi_name(place.getPoi_name())
                .category(place.getCategory())
                .ctprvn(place.getCtprvn())
                .sigungu(place.getSigungu())
                .legaldong(place.getLegaldong())
                .li_nm(place.getLi_nm())
                .lnbr_no(place.getLnbr_no())
                .buld_no(place.getBuld_no())
                .lc_lo(place.getLc_lo())
                .lc_la(place.getLc_la())
                .build();
    }
}
