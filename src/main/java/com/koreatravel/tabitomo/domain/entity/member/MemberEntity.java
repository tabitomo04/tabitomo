package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.UUID;

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
@ToString(exclude = {"password", "trips", "storyBooks", "additionalInfos", "likes"})
// Additional getters for compatibility with existing code
public class MemberEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Size(max = 80, message = "이메일은 최대 80자까지 입력 가능합니다.")
    @Column(name = "email", nullable = false, length = 80, unique = true)
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 60, message = "비밀번호는 8자 이상 60자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
             message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    @Column(name = "password", nullable = false, length = 60, columnDefinition = "VARCHAR(60) NOT NULL")
    private String password;

    @Transient
    private String confirmPassword;
    
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 45, message = "닉네임은 2자 이상 45자 이하로 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.")
    @Column(name = "nickname", nullable = false, length = 45, columnDefinition = "VARCHAR(45) NOT NULL")
    private String nickname;

    @Column(name = "date_of_birth", columnDefinition = "DATE")
    private LocalDate dateOfBirth;

    @Column(name = "gender", columnDefinition = "INT", nullable = false)
    private Integer gender;  // 1: Male, 2: Female, 3: Other, 4: Prefer not to say
    
    /**
     * Calculate and return the member's age based on date of birth
     * @return Age in years, or null if date of birth is not set
     */
    @Transient
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false, referencedColumnName = "country_id")
    private CountryEntity country;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_language_id", nullable = false, referencedColumnName = "id")
    private LanguageEntity preferredLanguage;
    
    @Column(name = "profile_image_url", length = 255, columnDefinition = "VARCHAR(255)")
    private String profileImageUrl;
    
    // Additional getters for compatibility with existing code
    public String getProfileImageUrl() {
        return this.profileImageUrl;
    }
    
    public String getNickname() {
        return this.nickname;
    }
    
    public String getEmail() {
        return this.email;
    }
    
    public String getGender() {
        if (this.gender == null) {
            return null;
        }
        switch (this.gender) {
            case 1: return "MALE";
            case 2: return "FEMALE";
            case 3: return "OTHER";
            case 4: return "PREFER_NOT_TO_SAY";
            default: return null;
        }
    }
    
    public CountryEntity getCountry() {
        return this.country;
    }

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
        joinColumns = @JoinColumn(name = "member_id", referencedColumnName = "id"),
        inverseJoinColumns = {
            @JoinColumn(name = "info_high_num", referencedColumnName = "info_high_num"),
            @JoinColumn(name = "info_low_num", referencedColumnName = "info_low_num")
        }
    )
    private Set<AddInfoEntity> additionalInfos = new HashSet<>();
    
    // Email verification is handled during registration/password reset without storing in DB
    
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

    @Column(name = "created_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "ENUM('ROLE_ADMIN','ROLE_USER') DEFAULT 'ROLE_USER'")
    @Builder.Default
    private MemberRole role = MemberRole.ROLE_USER;
    

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripEntity> trips = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StorybookEntity> storyBooks = new ArrayList<>();
    
    // FavoritePlaces are now managed in the trip package
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MemberAddInfoEntity> memberAddInfos = new ArrayList<>();
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.koreatravel.tabitomo.domain.entity.storybook.LikeEntity> likes = new ArrayList<>();
    
    /**
     * Adds a like to this member's likes list and sets up the bidirectional relationship.
     * @param like The like to add
     */
    public void addLike(com.koreatravel.tabitomo.domain.entity.storybook.LikeEntity like) {
        if (like != null) {
            like.setMember(this);
            this.likes.add(like);
        }
    }
    
    @Builder
    public MemberEntity(UUID id, String email, String password, String confirmPassword, String nickname, 
                       LocalDate dateOfBirth, Integer gender, CountryEntity country, LanguageEntity preferredLanguage, 
                       String profileImageUrl, Boolean isActive, Boolean questionnaireCompleted, 
                       Set<AddInfoEntity> additionalInfos, LocalDateTime createdAt, 
                       LocalDateTime updatedAt, List<TripEntity> trips, 
                       List<StorybookEntity> storyBooks, List<MemberAddInfoEntity> memberAddInfos,
                       List<com.koreatravel.tabitomo.domain.entity.storybook.LikeEntity> likes) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.nickname = nickname;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.country = country;
        this.preferredLanguage = preferredLanguage;
        this.profileImageUrl = profileImageUrl;
        this.isActive = isActive != null ? isActive : true;
        this.questionnaireCompleted = questionnaireCompleted != null ? questionnaireCompleted : false;
        this.additionalInfos = additionalInfos != null ? additionalInfos : new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.trips = trips != null ? trips : new ArrayList<>();
        this.storyBooks = storyBooks != null ? storyBooks : new ArrayList<>();
        this.memberAddInfos = memberAddInfos != null ? memberAddInfos : new ArrayList<>();
        this.likes = likes != null ? likes : new ArrayList<>();
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
    
    // Get the UUID ID
    public UUID getId() {
        return id;
    }
    
    public String getStatus() {
        return this.isActive ? "ACTIVE" : "INACTIVE";
    }
    
    @Transient
    public Integer getCountryId() {
        return this.country != null ? this.country.getCountryId() : null;
    }
    
    @Transient
    public Integer getPreferredLanguageId() {
        return this.preferredLanguage != null ? this.preferredLanguage.getLanguageId() : null;
    }
    
    public boolean isLocked() {
        return !this.isActive();
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

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
