package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Entity
@Table(name = "country")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryEntity {
    @Id
    @Column(name = "country_id", nullable = false, columnDefinition = "INT")
    private Integer countryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lang_id")
    private LanguageEntity language;

    @Column(name = "country_name", nullable = false, length = 50)
    private String countryName;

    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "iso_code", nullable = false, length = 3)
    private String isoCode;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MemberEntity> members = new ArrayList<>();
}
