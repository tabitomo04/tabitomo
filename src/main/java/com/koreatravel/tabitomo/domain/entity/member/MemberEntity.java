package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import com.koreatravel.tabitomo.domain.entity.trip.TripEntity;

@Entity
@Table(name = "Member",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "nickname"),
        @UniqueConstraint(columnNames = "verificationToken")
    })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "trips", "storyBooks"})
public class MemberEntity {
    @Id
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Size(max = 80, message = "이메일은 최대 80자까지 입력 가능합니다.")
    @Column(name = "email", nullable = false, length = 80)
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 60, message = "비밀번호는 8자 이상 60자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
             message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    @Column(nullable = false, length = 60)
    private String password;

    @Transient
    private String confirmPassword;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 45, message = "닉네임은 2자 이상 45자 이하로 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.")
    @Column(nullable = false, length = 45)
    private String nickname;

    @Column(nullable = false)
    private Integer gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private CountryEntity country;
    
    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "email_verify_token", length = 64)
    private String emailVerifyToken;
    
    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = false; // Default to false until email is verified
    
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MemberRole role = MemberRole.ROLE_USER;
    
    @Column(name = "password_reset_token", length = 64)
    private String passwordResetToken;
    
    @Column(name = "password_reset_expires")
    private LocalDateTime passwordResetExpires;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripEntity> trips = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StorybookEntity> storyBooks = new ArrayList<>();
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FavoritePlaceEntity> favoritePlaces = new ArrayList<>();
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserSelectedInfoEntity> userSelectedInfos = new ArrayList<>();
    
    public enum MemberRole {
        ROLE_USER, ROLE_ADMIN
    }
    
    public boolean isPasswordMatching() {
        return this.password != null && this.password.equals(this.confirmPassword);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Generate email verification token on user creation
        if (this.emailVerifyToken == null) {
            this.emailVerifyToken = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
