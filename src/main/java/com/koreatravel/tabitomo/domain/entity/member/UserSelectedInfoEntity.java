package com.koreatravel.tabitomo.domain.entity.member;

import com.koreatravel.tabitomo.id.UserSelectedInfoId;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_selected_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserSelectedInfoId.class)
public class UserSelectedInfoEntity {
    @Id
    @Column(name = "info_high_num", nullable = false, columnDefinition = "INT")
    private Integer infoHighNum;

    @Id
    @Column(name = "info_low_num", nullable = false, columnDefinition = "INT")
    private Integer infoLowNum;

    @Id
    @Column(name = "email", nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "info_high_num", referencedColumnName = "info_high_num", insertable = false, updatable = false),
        @JoinColumn(name = "info_low_num", referencedColumnName = "info_low_num", insertable = false, updatable = false)
    })
    private AddInfoEntity addInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", referencedColumnName = "email", insertable = false, updatable = false)
    private MemberEntity member;
}