package com.koreatravel.tabitomo.domain.dto.member;

import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer infoHighNum;
    private Integer infoLowNum;
    private String infoName;
    private String content;

    public static AddInfoDTO fromEntity(AddInfoEntity entity) {
        return AddInfoDTO.builder()
                .infoHighNum(entity.getInfoHighNum())
                .infoLowNum(entity.getInfoLowNum())
                .infoName(entity.getInfoName())
                .content(entity.getContent())
                .build();
    }
}
