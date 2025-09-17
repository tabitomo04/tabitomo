package com.koreantravel.tabitomo.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Member")
public class Member {

    @Id
    @Column(name = "email", length = 80)
    private String email;

    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Column(name = "gender", nullable = false)
    private byte gender;

    @Column(name = "nickname", length = 45, nullable = false, unique = true)
    private String nickname;

    @Column(name = "country_id", nullable = false)
    private int countryId;

    @Column(name = "isActive", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "role", length = 20, nullable = false)
    private String role = "user";

    @Builder
    public Member(String email, String password, byte gender, String nickname, int countryId) {
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.nickname = nickname;
        this.countryId = countryId;
    }

    //== Admin Update Methods ==//
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateRole(String role) {
        this.role = role;
    }

    public void updateIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
