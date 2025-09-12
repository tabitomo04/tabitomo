package com.koreatravel.tabitomo.domain.dto.storybook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SaveRequestDTO {
    private Integer booknum;
    private Integer tempId;
    private String savetype;
    private String title;
    private String subtitle;
    private String content;
    private String email;
    
    // Builder pattern implementation
    public static SaveRequestDTOBuilder builder() {
        return new SaveRequestDTOBuilder();
    }
}
