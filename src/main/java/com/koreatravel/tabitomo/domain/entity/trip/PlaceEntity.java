package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "place")
public class PlaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "category_code", length = 20)
    private String categoryCode;

    @Column(length = 200)
    private String address;

    @Column(length = 80)
    private String city;

    @Column(length = 80)
    private String region;

    @OneToMany(mappedBy = "place")
    private List<ScheduleEntity> schedules = new ArrayList<>();

    @Builder
    public PlaceEntity(String name, String categoryCode, String address, String city, String region) {
        this.name = name;
        this.categoryCode = categoryCode;
        this.address = address;
        this.city = city;
        this.region = region;
    }
}
