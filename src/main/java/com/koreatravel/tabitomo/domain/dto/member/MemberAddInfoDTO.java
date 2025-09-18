package com.koreatravel.tabitomo.domain.dto.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for member additional information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAddInfoDTO {
    private String memberId;
    private List<AddInfoDTO> addInfo;
    private LocalDateTime createdAt;

    /**
     * Convert a list of MemberAddInfoEntity to MemberAddInfoDTOs
     */
    public static List<MemberAddInfoDTO> fromEntities(List<MemberAddInfoEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(MemberAddInfoDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert a single MemberAddInfoEntity to MemberAddInfoDTO
     */
    public static MemberAddInfoDTO fromEntity(MemberAddInfoEntity entity) {
        if (entity == null) {
            return null;
        }
        
        MemberAddInfoDTO memberAddInfoDTO = MemberAddInfoDTO.builder()
                .memberId(entity.getMemberId().toString())
                .createdAt(entity.getCreatedAt())
                .build();
        memberAddInfoDTO.setAddInfo(List.of(AddInfoDTO.fromEntity(entity.getAddInfo())));
        return memberAddInfoDTO;
    }
}
