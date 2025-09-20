package com.koreatravel.tabitomo.config.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class UserDetailsImpl implements UserDetails {

    // Re-applying the correct serialVersionUID to resolve session deserialization issues.
    @Serial
    private static final long serialVersionUID = 4215309437416150371L;

    private final MemberEntity member;
    private final List<MemberAddInfoEntity> additionalInfo;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(MemberEntity member, List<MemberAddInfoEntity> additionalInfo) {
        this.member = member;
        this.additionalInfo = additionalInfo != null ? additionalInfo : Collections.emptyList();
        this.authorities = Collections.singletonList(
            new SimpleGrantedAuthority(member.getRole())
        );
    }

    public String getEmail() {
        return member.getEmail();
    }

    public String getNickname() {
        return member.getNickname();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    /**
     * Get additional member information (hashtags, etc.)
     * @return List of additional member information
     */
    public List<MemberAddInfoEntity> getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getNickname();
    }

    @JsonIgnore
    public UUID getId() {
        return member.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return member.isActive();
    }

    // Additional profile information
    public UUID getUserId() {
        return member.getId();
    }

    public String getProfileImageUrl() {
        return member.getProfileImageUrl();
    }

    public Integer getAge() {
        if (member.getDateOfBirth() == null) {
            return null;
        }
        return Period.between(member.getDateOfBirth(), LocalDate.now()).getYears();
    }

    public Integer getGender() {
        return member.getGender();
    }

    public String getCountryCode() {
        return member.getCountry() != null ? member.getCountry().getCountryCode() : null;
    }

    public String getNativeLanguageName() {
        return member.getPreferredLanguage() != null ? 
               member.getPreferredLanguage().getNameNative() : null;
    }

    // For backward compatibility with existing code
    @JsonIgnore
    public UUID getMemberId() {
        return member.getId();
    }
}
