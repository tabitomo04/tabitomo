package com.koreatravel.tabitomo.domain.dto;

import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private String email;
    private String password;
    private String name;
    private String nickname;
    private int countryId;
    private String role;
    private boolean isActive;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MemberEntity toEntity() {
        return MemberEntity.builder()
                .email(email)
                .password(password)
                .name(name)
                .nickname(nickname)
                .countryId(countryId)
                .role(role != null ? role : "ROLE_USER")
                .isActive(isActive)
                .status(status)
                .createdAt(createdAt != null ? createdAt.toString() : null)
                .updatedAt(updatedAt != null ? updatedAt.toString() : null)
                .build();
    }

    public static MemberDTO fromEntity(MemberEntity member) {
        return MemberDTO.builder()
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .countryId(member.getCountryId())
                .role(member.getRole())
                .isActive(member.isActive())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt() != null ? LocalDateTime.parse(member.getCreatedAt()) : null)
                .updatedAt(member.getUpdatedAt() != null ? LocalDateTime.parse(member.getUpdatedAt()) : null)
                .build();
    }
}
