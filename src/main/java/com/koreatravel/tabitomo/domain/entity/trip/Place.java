package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    private String categoryCode; // 예: ATTRACTION, RESTAURANT, ACCOMMODATION

    private String address;

    private String city; //사용안함.

    private String region; // 사용안함

    private Double latitude;

    private Double longitude;

    private String imageUrl;

    @Lob
    private String description;

    private String restDate; // 사용안함

    private String useTime; // 사용안함

    private String priceRange;


    @Builder
    public Place(String name, String categoryCode, String address, String city, String region, Double latitude, Double longitude, String imageUrl, String description, String restDate, String useTime, String priceRange) {
        this.name = name;
        this.categoryCode = categoryCode;
        this.address = address;
        this.city = city;  // 사용안함
        this.region = region;  // 사용안함
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.description = description;
        this.restDate = restDate;  // 사용안함
        this.useTime = useTime; // 사용안함
        this.priceRange = priceRange;
    }
}
