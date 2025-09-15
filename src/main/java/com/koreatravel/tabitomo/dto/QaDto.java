package com.koreatravel.tabitomo.dto;

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