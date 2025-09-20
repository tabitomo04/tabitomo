package com.koreatravel.tabitomo.domain.entity.trip;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    private String categoryCode; // 예: ATTRACTION, RESTAURANT, ACCOMMODATION

    private String address;

    private String city;

    private String region;

    private Double latitude;

    private Double longitude;

    private String imageUrl;

    @Lob
    private String description;

    private String priceRange;

    @Builder
    public Place(String name, String categoryCode, String address, String city, String region, Double latitude, Double longitude, String imageUrl, String description, String priceRange) {
        this.name = name;
        this.categoryCode = categoryCode;
        this.address = address;
        this.city = city;
        this.region = region;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.description = description;
        this.priceRange = priceRange;
    }
}
