package com.koreatravel.tabitomo.dto.member;

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
public class AddInfoDto {
    private Integer infoHighNum;
    private Integer infoLowNum;
    private String content;
    private String infoName;
    
    public static AddInfoDto fromEntity(com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity entity) {
        return AddInfoDto.builder()
                .infoHighNum(entity.getInfoHighNum())
                .infoLowNum(entity.getInfoLowNum())
                .content(entity.getContent())
                .infoName(entity.getInfoName())
                .build();
    }
}
