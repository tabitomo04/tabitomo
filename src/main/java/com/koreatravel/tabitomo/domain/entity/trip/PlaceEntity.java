package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "place")
public class PlaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id", columnDefinition = "BIGINT NOT NULL AUTO_INCREMENT")
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "category_code", length = 20)
    private String categoryCode;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "city", length = 80)
    private String city;

    @Column(name = "region", length = 80)
    private String region;

    @OneToMany(mappedBy = "place")
    @Builder.Default
    private List<ScheduleEntity> schedules = new ArrayList<>();
    
    @Builder
    private PlaceEntity(String name, String categoryCode, String address, String city, String region) {
        this.name = name;
        this.categoryCode = categoryCode;
        this.address = address;
        this.city = city;
        this.region = region;
        this.schedules = new ArrayList<>();
    }
}
