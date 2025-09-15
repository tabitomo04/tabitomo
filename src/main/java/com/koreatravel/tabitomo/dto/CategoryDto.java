package com.koreatravel.tabitomo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryDto {
    private Integer id;
    private String nameKo;
    private String nameEn;
    private String nameJa;
}