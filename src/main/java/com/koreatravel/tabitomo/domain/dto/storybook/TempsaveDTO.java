package com.koreatravel.tabitomo.domain.dto.storybook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class TempsaveDTO {
    private Integer tempId;
    private String title;
    private String subtitle;
    private String content;
    private LocalDateTime saveTime;
    private Integer booknum;
    private String status;

    // Builder pattern implementation
    public static TempsaveDTOBuilder builder() {
        return new TempsaveDTOBuilder();
    }
}
