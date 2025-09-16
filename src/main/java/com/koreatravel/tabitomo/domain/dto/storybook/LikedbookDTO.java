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
public class LikedbookDTO {

    private Integer likeId;
    private Integer booknum;
    private String email;
    private LocalDateTime createDate;
}

