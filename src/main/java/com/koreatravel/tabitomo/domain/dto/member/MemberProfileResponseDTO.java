package com.koreatravel.tabitomo.domain.dto.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for member profile response that includes both basic profile and additional information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileResponseDTO {
    private MemberProfileDTO profile;
    private List<MemberAddInfoDTO> additionalInfo;
    private boolean isCurrentUser;
    
    /**
     * Static factory method to create a response DTO
     */
    public static MemberProfileResponseDTO of(MemberProfileDTO profile, List<MemberAddInfoEntity> additionalInfo, boolean isCurrentUser) {
        List<MemberAddInfoDTO> addInfoDTO = additionalInfo.stream()
                .map(MemberAddInfoDTO::fromEntity)
                .toList();
        MemberProfileResponseDTO memberProfileResponseDTO = MemberProfileResponseDTO.builder()
                .profile(profile)
                .additionalInfo(addInfoDTO)
                .isCurrentUser(isCurrentUser)
                .build();
        return memberProfileResponseDTO;
    }
}
