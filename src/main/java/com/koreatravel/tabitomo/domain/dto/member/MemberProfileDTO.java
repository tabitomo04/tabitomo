package com.koreatravel.tabitomo.domain.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileDTO {
    private UUID id;
    private String email;
    private String nickname;
    private String gender;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private String role;
    
    // Reference data
    private Long countryId;
    private String countryName;
    private Long preferredLanguageId;
    private String preferredLanguageName;
}
