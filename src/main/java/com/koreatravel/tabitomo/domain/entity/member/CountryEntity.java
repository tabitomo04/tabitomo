package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Country")
public class CountryEntity {
    @Id
    private int country_id;

    @Column
    private int lang_id;

    @Column
    private String country_name;

    @Column
    private String region;

    @Column
    private String iso_code;
}
