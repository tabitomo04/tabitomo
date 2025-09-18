package com.koreatravel.tabitomo.domain.dto.storybook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TempsaveDTO {
    private Integer tempId;
    private String title;
    private String subtitle;
    private String content;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String tags;
    private UUID memberId;
    private String nickname;
}
