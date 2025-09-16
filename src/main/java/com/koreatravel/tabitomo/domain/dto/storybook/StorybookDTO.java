package com.koreatravel.tabitomo.domain.dto.storybook;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StorybookDTO {
    private Integer booknum;
    private String title;
    private String subtitle;
    private String content;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private Integer likes;

}
