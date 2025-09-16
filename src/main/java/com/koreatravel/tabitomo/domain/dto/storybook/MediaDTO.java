package com.koreatravel.tabitomo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MediaDTO {

    private Integer id;
    private Integer num;
    private String status;
    private String mediaUrl;
    private String mediaType;
    private LocalDateTime uploadTime;
}