package com.koreatravel.tabitomo.domain.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "Place")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "poi_id")
    private String poi_id;

    @Column(name = "poi_name")
    private String poi_name;

    @Column(name = "category")
    private String category;

    @Column(name = "ctprvn")
    private String ctprvn;

    @Column(name = "sigungu")
    private String sigungu;

    @Column(name = "legaldong")
    private String legaldong;

    @Column(name = "li_nm")
    private String li_nm;

    @Column(name = "lnbr_no")
    private String lnbr_no;

    @Column(name = "buld_no")
    private String buld_no;

    @Column(name = "lc_lo")
    private String lc_lo;

    @Column(name = "lc_la")
    private String lc_la;
}
