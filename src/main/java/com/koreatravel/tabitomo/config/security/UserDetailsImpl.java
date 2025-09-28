package com.koreatravel.tabitomo.config.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class UserDetailsImpl implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;
    
    private final UUID id;
    private final String email;
    private final String nickname;
    private final String password;
    private final boolean active;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(MemberEntity member, List<?> additionalInfo) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.password = member.getPassword();
        this.active = member.isActive();
        this.role = member.getRole();
        this.authorities = Collections.singletonList(
            new SimpleGrantedAuthority(member.getRole())
        );
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }
    
    public UUID getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities != null ? authorities : Collections.emptyList();
    }
    
    // This method is kept for backward compatibility
    public Object getMember() {
        return null;
    }
    
    /**
     * Get additional member information (hashtags, etc.)
     * @return Empty list as we're not storing additional info in session
     */
    public List<?> getAdditionalInfo() {
        return Collections.emptyList();
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return nickname;
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

    // Additional profile information
    public UUID getUserId() {
        return id;
    }

    public String getProfileImageUrl() {
        // This would need to be handled differently since we're not storing the member entity
        return null;
    }

    public Integer getAge() {
        // Date of birth is no longer stored in the session
        return null;
    }

    public Integer getGender() {
        // Gender is no longer stored in the session
        return null;
    }

    public String getCountryCode() {
        // Country info is no longer stored in the session
        return null;
    }

    public String getNativeLanguageName() {
        return null;
    }

    // For backward compatibility with existing code
    @JsonIgnore
    public UUID getMemberId() {
        return id;
    }

    @JsonIgnore
    public String getMbti() {
        // MBTI is no longer stored in the session
        return null;
    }
    
    /**
     * Check if the user has completed the questionnaire
     * @return true if the user has completed the questionnaire, false otherwise
     */
    public boolean isQuestionnaireCompleted() {
        // Default implementation - you may need to implement the actual logic
        // based on how you track questionnaire completion in your application
        return false;
    }
}
