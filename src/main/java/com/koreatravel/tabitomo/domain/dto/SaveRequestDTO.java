package com.koreatravel.tabitomo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SaveRequestDTO {
    private Integer booknum;
    private Integer tempId;
    private List<String> tags;
    private String temptags; // 임시저장 시 태그
    private String savetype;
    private String title;
    private String subtitle;
    private String content;
}
