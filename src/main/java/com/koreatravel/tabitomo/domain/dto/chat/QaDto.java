package com.koreatravel.tabitomo.domain.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class QaDto {
    private Integer qaId;
    private String questionKo;
    private String questionEn;
    private String questionJa;
}