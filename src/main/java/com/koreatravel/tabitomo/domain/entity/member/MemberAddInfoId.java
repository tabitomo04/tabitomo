package com.koreatravel.tabitomo.domain.entity.member;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class MemberAddInfoId implements Serializable {
    private Long memberId;
    private Integer infoHighNum;
    private Integer infoLowNum;
    
    public MemberAddInfoId() {}
    
    public MemberAddInfoId(Long memberId, Integer infoHighNum, Integer infoLowNum) {
        this.memberId = memberId;
        this.infoHighNum = infoHighNum;
        this.infoLowNum = infoLowNum;
    }
}
