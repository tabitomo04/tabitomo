package com.koreatravel.tabitomo.config.security;

import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Custom UserDetails implementation that wraps a MemberEntity and provides
 * the necessary information for Spring Security authentication.
 */
@Getter
public class MemberDetails implements UserDetails {
    private static final long serialVersionUID = 1L;
    
    private final String username;
    private final String password;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean credentialsNonExpired;
    private final boolean accountNonLocked;
    private final Collection<? extends GrantedAuthority> authorities;
    
    // Reference to the actual member entity
    private final MemberEntity member;
    
    // Cached profile DTO for quick access
    private transient MemberProfileDTO profile;

    /**
     * Constructor for MemberDetails
     */
    public MemberDetails(
            String username,
            String password,
            boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities,
            MemberEntity member) {
        
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.authorities = authorities;
        this.member = member;
    }
    
    /**
     * Get the user's profile information as a DTO
     */
    public MemberProfileDTO getProfile() {
        if (profile == null && member != null) {
            profile = MemberProfileDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .gender(member.getGenderAsString())
                .profileImageUrl(member.getProfileImageUrl())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .status(member.getStatus())
                .role(member.getRole().name())
                .countryId(member.getCountryId() != null ? member.getCountryId().intValue() : null)
                .preferredLanguageId(member.getPreferredLanguageId() != null ? member.getPreferredLanguageId().intValue() : null)
                .build();
        }
        return profile;
    }
}
