package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

@Entity
@Table(name = "language")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageEntity {
    @Id
    @Column(name = "lang_id", nullable = false, columnDefinition = "INT")
    private Integer langId;

    @Column(name = "lang_code", nullable = false, length = 10)
    private String langCode;

    @Column(name = "lang_name", nullable = false, length = 50)
    private String langName;

    @OneToMany(mappedBy = "language", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CountryEntity> countries = new ArrayList<>();
}
