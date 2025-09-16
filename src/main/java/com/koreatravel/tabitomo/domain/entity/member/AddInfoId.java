package com.koreatravel.tabitomo.domain.entity.member;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class AddInfoId implements Serializable {
    private Integer infoHighNum;
    private Integer infoLowNum;
    
    public AddInfoId() {}
    
    public AddInfoId(Integer infoHighNum, Integer infoLowNum) {
        this.infoHighNum = infoHighNum;
        this.infoLowNum = infoLowNum;
    }
}
