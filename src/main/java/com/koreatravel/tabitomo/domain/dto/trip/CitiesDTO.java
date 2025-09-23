package com.koreatravel.tabitomo.domain.dto.trip;

import com.koreatravel.tabitomo.domain.entity.trip.CitiesEntity;
import lombok.Data;

@Data
public class CitiesDTO {
    private Integer id;
    private String type;
    private String region;
    private String city;
    private String description;
    private String image_url;
    
    public CitiesDTO(CitiesEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.type = entity.getType();
            this.region = entity.getRegion();
            this.city = entity.getCity();
            this.description = entity.getDescription();
            this.image_url = entity.getImageUrl();
        }
    }
}