package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "cities")
@Getter
public class CitiesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column
    private String type;

    @Column
    private String region;

    @Column
    private String city;

    @Column
    private String description;

    @Column(name = "image_url")
    private String imageUrl;
}
