package com.koreatravel.tabitomo.domain.dto.trip;

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
}
