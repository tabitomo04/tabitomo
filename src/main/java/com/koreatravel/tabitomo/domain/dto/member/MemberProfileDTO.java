package com.koreatravel.tabitomo.domain.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private String dateOfBirth; // YYYY-MM-DD format
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private String role;
    
    // Reference data
    private Integer countryId;
    private String countryName;
    private Integer preferredLanguageId;
    private String preferredLanguageName;
    
    // Custom builder to handle dateOfBirth conversion if needed
    public static class MemberProfileDTOBuilder {
        private String dateOfBirth;
        
        public MemberProfileDTOBuilder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth != null ? dateOfBirth.toString() : null;
            return this;
        }
        
        public MemberProfileDTOBuilder dateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }
    }
}
