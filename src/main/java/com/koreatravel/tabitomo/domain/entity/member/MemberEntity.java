package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import com.koreatravel.tabitomo.domain.entity.trip.TripEntity;

@Entity
@Table(name = "member",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "nickname")
    })
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "trips", "storyBooks", "additionalInfos"})
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
    @Column(name = "password", nullable = false, length = 60, columnDefinition = "VARCHAR(60) NOT NULL")
    private String password;

    @Transient
    private String confirmPassword;
    
    @Column(name = "email_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean emailVerified;
    
    @Column(name = "email_verify_token", length = 64)
    private String emailVerifyToken;
    
    @Column(name = "email_verify_token_expiry")
    private LocalDateTime emailVerifyTokenExpiry;
    
    @Column(name = "email_verification_token", length = 64)
    private String emailVerificationToken;
    
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 45, message = "닉네임은 2자 이상 45자 이하로 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.")
    @Column(name = "nickname", nullable = false, length = 45, columnDefinition = "VARCHAR(45) NOT NULL")
    private String nickname;

    @Column(name = "age", columnDefinition = "INT")
    private Integer age;

    @Column(name = "gender", columnDefinition = "INT", nullable = false)
    private Integer gender;  // 1: Male, 2: Female, 3: Other, 4: Prefer not to say

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private CountryEntity country;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_language_id", nullable = false)
    private LanguageEntity preferredLanguage;
    
    @Column(name = "profile_image_url", length = 255, columnDefinition = "VARCHAR(255)")
    private String profileImageUrl;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isActive = true;
    
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    @Builder.Default
    @Column(name = "questionnaire_completed", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean questionnaireCompleted = false;
    
    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "member_add_info",
        joinColumns = @JoinColumn(name = "email", referencedColumnName = "email"),
        inverseJoinColumns = {
            @JoinColumn(name = "info_high_num", referencedColumnName = "info_high_num"),
            @JoinColumn(name = "info_low_num", referencedColumnName = "info_low_num")
        }
    )
    private Set<AddInfoEntity> additionalInfos = new HashSet<>();
    
    // Helper method to add additional info
    public void addAdditionalInfo(AddInfoEntity addInfo) {
        this.additionalInfos.add(addInfo);
    }
    
    // Helper method to check if member has a specific additional info
    public boolean hasAdditionalInfo(AddInfoEntity addInfo) {
        return this.additionalInfos.contains(addInfo);
    }
    
    // Mark questionnaire as completed
    public void completeQuestionnaire() {
        this.questionnaireCompleted = true;
    }
    
    // Check if questionnaire is completed
    public boolean isQuestionnaireCompleted() {
        return Boolean.TRUE.equals(this.questionnaireCompleted);
    }

    @Column(name = "reset_token", length = 36)
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "created_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "ENUM('ROLE_ADMIN','ROLE_USER') DEFAULT 'ROLE_USER'")
    @Builder.Default
    private MemberRole role = MemberRole.ROLE_USER;
    
    @Column(name = "password_reset_token", length = 64, columnDefinition = "VARCHAR(64)")
    private String passwordResetToken;
    
    @Column(name = "password_reset_expires", columnDefinition = "DATETIME(6)")
    private LocalDateTime passwordResetExpires;
    
    @Column(name = "last_login_time", columnDefinition = "DATETIME(6)")
    private LocalDateTime lastLoginTime;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripEntity> trips = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StorybookEntity> storyBooks = new ArrayList<>();
    
    // FavoritePlaces are now managed in the trip package
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserSelectedInfoEntity> userSelectedInfos = new ArrayList<>();
    
    @Builder
    public MemberEntity(String email, String password, String confirmPassword, boolean emailVerified, 
                       String emailVerifyToken, LocalDateTime emailVerifyTokenExpiry, String nickname, 
                       Integer age, Integer gender, CountryEntity country, LanguageEntity preferredLanguage, 
                       String profileImageUrl, Boolean isActive, Boolean questionnaireCompleted, 
                       Set<AddInfoEntity> additionalInfos, String resetToken, LocalDateTime resetTokenExpiry, 
                       LocalDateTime createdAt, LocalDateTime updatedAt, MemberRole role, 
                       String passwordResetToken, LocalDateTime passwordResetExpires, 
                       List<TripEntity> trips, List<StorybookEntity> storyBooks, 
                       List<UserSelectedInfoEntity> userSelectedInfos, String emailVerificationToken) {
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.emailVerified = emailVerified;
        this.emailVerifyToken = emailVerifyToken;
        this.emailVerifyTokenExpiry = emailVerifyTokenExpiry;
        this.nickname = nickname;
        this.age = age;
        this.gender = gender;
        this.country = country;
        this.preferredLanguage = preferredLanguage;
        this.profileImageUrl = profileImageUrl;
        this.isActive = isActive != null ? isActive : true;
        this.questionnaireCompleted = questionnaireCompleted != null ? questionnaireCompleted : false;
        this.additionalInfos = additionalInfos != null ? additionalInfos : new HashSet<>();
        this.resetToken = resetToken;
        this.resetTokenExpiry = resetTokenExpiry;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.role = role != null ? role : MemberRole.ROLE_USER;
        this.passwordResetToken = passwordResetToken;
        this.passwordResetExpires = passwordResetExpires;
        this.trips = trips != null ? trips : new ArrayList<>();
        this.storyBooks = storyBooks != null ? storyBooks : new ArrayList<>();
        this.userSelectedInfos = userSelectedInfos != null ? userSelectedInfos : new ArrayList<>();
        this.emailVerificationToken = emailVerificationToken;
    }
    
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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Additional methods needed for security
    public Long getId() {
        // Since email is the ID, we can return a hash of it as a long
        return (long) this.email.hashCode();
    }
    
    public String getStatus() {
        return this.isActive ? "ACTIVE" : "INACTIVE";
    }
    
    public Long getCountryId() {
        return this.country != null ? this.country.getCountryId() : null;
    }
    
    public Long getPreferredLanguageId() {
        return this.preferredLanguage != null ? this.preferredLanguage.getLanguageId() : null;
    }
    
    public boolean isLocked() {
        return !this.isActive();
    }
    
    public boolean isEmailVerified() {
        return this.emailVerified;
    }
    
    // Convert gender from Integer to String representation
    public String getGenderAsString() {
        if (this.gender == null) {
            return null;
        }
        return switch (this.gender) {
            case 1 -> "MALE";
            case 2 -> "FEMALE";
            case 3 -> "OTHER";
            case 4 -> "PREFER_NOT_TO_SAY";
            default -> null;
        };
    }
    
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    public void setPasswordResetExpiry(LocalDateTime expiry) {
        this.passwordResetExpires = expiry;
    }
    
    public void clearEmailVerificationToken() {
        this.emailVerificationToken = null;
        this.emailVerifyTokenExpiry = null;
    }
    
    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetExpires = null;
    }
    
    public LocalDateTime getEmailVerifyTokenExpiry() {
        return emailVerifyTokenExpiry;
    }
    
    public void setEmailVerifyTokenExpiry(LocalDateTime expiry) {
        this.emailVerifyTokenExpiry = expiry;
    }
    
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerifyTokenExpiry = null;
    }
    
    public void setEmailVerificationToken(String token) {
        this.emailVerificationToken = token;
        this.emailVerifyTokenExpiry = LocalDateTime.now().plusDays(1); // 24 hours expiry
    }
    
    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }
    
    public void setPasswordResetToken(String token) {
        this.passwordResetToken = token;
        this.passwordResetExpires = LocalDateTime.now().plusHours(24); // 24 hours expiry
    }
    
    public String getPasswordResetToken() {
        return passwordResetToken;
    }
    
    public LocalDateTime getPasswordResetExpiry() {
        return passwordResetExpires;
    }
    
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.passwordResetToken = null;
        this.passwordResetExpires = null;
    }
}
