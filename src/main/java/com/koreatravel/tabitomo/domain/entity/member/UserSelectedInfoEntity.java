package com.koreatravel.tabitomo.domain.entity.member;

import com.koreatravel.tabitomo.id.UserSelectedInfoId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "UserSelectedInfo")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserSelectedInfoId.class)
public class UserSelectedInfoEntity {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "infohighnum", referencedColumnName = "infohighnum"),
        @JoinColumn(name = "infolownum", referencedColumnName = "infolownum")
    })
    private AddInfoEntity addInfo;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    private MemberEntity member;
}