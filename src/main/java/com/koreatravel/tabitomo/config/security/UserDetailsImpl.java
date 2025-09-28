package com.koreatravel.tabitomo.config.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Custom UserDetails implementation for Spring Security.
 * This class represents the authenticated user's principal and contains user details.
 */
@Getter
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;
    
    private final UUID id;
    private final String email;
    private final String nickname;
    private final boolean active;
    private final String role;
    private final boolean questionnaireCompleted;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(MemberEntity member, boolean hasCompletedQuestionnaire) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.active = member.isActive();
        this.role = member.getRole();
        this.questionnaireCompleted = hasCompletedQuestionnaire;
        this.authorities = Collections.singletonList(
            new SimpleGrantedAuthority(member.getRole())
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return null; // Password should not be exposed
    }

    @Override
    public String getUsername() {
        return email;
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
        return active;
    }

    // Additional methods
    public UUID getMemberId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return "/images/default-profile.png";
    }

    public Integer getGender() {
        return 0; // 0: unspecified, 1: male, 2: female
    }

    public String getCountryCode() {
        return null;
    }

    public String getNativeLanguageName() {
        return null;
    }

    @JsonIgnore
    public String getMbti() {
        return null;
    }

    public boolean isQuestionnaireCompleted() {
        return this.questionnaireCompleted;
    }

    // For updating questionnaire status
    public UserDetailsImpl withQuestionnaireCompleted(boolean completed) {
        return new UserDetailsImpl(this, completed);
    }

    // Private constructor for creating copies with updated fields
    private UserDetailsImpl(UserDetailsImpl original, boolean questionnaireCompleted) {
        this.id = original.id;
        this.email = original.email;
        this.nickname = original.nickname;
        this.active = original.active;
        this.role = original.role;
        this.authorities = original.authorities;
        this.questionnaireCompleted = questionnaireCompleted;
    }
}
