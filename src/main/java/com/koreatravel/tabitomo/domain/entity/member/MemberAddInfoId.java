package com.koreatravel.tabitomo.domain.entity.member;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
public class MemberAddInfoId implements Serializable {
    private UUID memberId;
    private Integer infoHighNum;
    private Integer infoLowNum;
    
    public MemberAddInfoId() {}
    
    public MemberAddInfoId(UUID memberId, Integer infoHighNum, Integer infoLowNum) {
        this.memberId = memberId;
        this.infoHighNum = infoHighNum;
        this.infoLowNum = infoLowNum;
    }
}
