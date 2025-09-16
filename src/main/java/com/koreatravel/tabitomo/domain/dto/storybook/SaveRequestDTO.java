package com.koreatravel.tabitomo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SaveRequestDTO {
    private Integer booknum;
    private Integer tempId;
    private String savetype;
    private String title;
    private String subtitle;
    private String content;
}
