package com.koreatravel.tabitomo.domain.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddInfoDTO {
    private Integer infoHighNum;
    private Integer infoLowNum;
    private String content;
    private String infoName;
    
    public static AddInfoDTO fromEntity(com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity entity) {
        return AddInfoDTO.builder()
                .infoHighNum(entity.getInfoHighNum())
                .infoLowNum(entity.getInfoLowNum())
                .content(entity.getContent())
                .infoName(entity.getInfoName())
                .build();
    }
}
