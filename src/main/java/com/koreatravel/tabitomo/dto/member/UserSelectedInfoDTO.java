package com.koreatravel.tabitomo.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for saving user's selected information to the database
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSelectedInfoDTO {
    private String email;
    private Integer infoHighNum;
    private Integer infoLowNum;
}
