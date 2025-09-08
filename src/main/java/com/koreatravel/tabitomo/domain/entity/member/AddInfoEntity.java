package com.koreatravel.tabitomo.domain.entity.member;

import com.koreatravel.tabitomo.id.AddInfoId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@Table(name = "AddInfo")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(AddInfoId.class)
public class AddInfoEntity {
    @Id
    @Column(name = "infohighnum", nullable = false)
    private Integer infoHighNum;

    @Id
    @Column(name = "infolownum", nullable = false)
    private Integer infoLowNum;

    @Column(nullable = false, length = 100)
    private String infoName;

    @OneToMany(mappedBy = "addInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSelectedInfoEntity> userSelectedInfos = new ArrayList<>();
}