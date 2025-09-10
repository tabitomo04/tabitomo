package com.koreatravel.tabitomo.security.jwt;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation for Spring Security authentication.
 * This class wraps the MemberEntity and provides the necessary information for authentication.
 */
public class CustomUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;
    
    private final MemberEntity member;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(MemberEntity member) {
        this.member = member;
        // For now, all users have the ROLE_USER authority
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Account never expires
    }

    @Override
    public boolean isAccountNonLocked() {
        return !member.isLocked(); // Check if account is not locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials never expire
    }

    @Override
    public boolean isEnabled() {
        return member.isActive() && member.isEmailVerified();
    }

    /**
     * Get the underlying MemberEntity
     */
    public MemberEntity getMember() {
        return member;
    }
    
    /**
     * Convert the user details to a profile DTO for session storage
     */
    public MemberProfileDTO toProfileDTO() {
        return MemberProfileDTO.builder()
            .id(member.getId())
            .email(member.getEmail())
            .nickname(member.getNickname())
            .gender(member.getGenderAsString())
            .profileImageUrl(member.getProfileImageUrl())
            .createdAt(member.getCreatedAt())
            .updatedAt(member.getUpdatedAt())
            .status(member.getStatus())
            .role(member.getRole().name())
            .countryId(member.getCountryId())
            .preferredLanguageId(member.getPreferredLanguageId())
            .build();
    }
}
