package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

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
public class MemberEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Email
    @NotBlank
    @Size(max = 80)
    private String email;

    @NotNull(message = "생년월일은 필수 입력 값입니다.")
    @Past(message = "유효한 생년월일을 입력해주세요.")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, unique = true, length = 45)
    private String nickname;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private boolean active = true;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;
    
    @Column(name = "questionnaire_completed", nullable = false)
    @Builder.Default
    private boolean questionnaireCompleted = false;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "role")
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", referencedColumnName = "country_id")
    private CountryEntity country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_language_id", referencedColumnName = "id")
    private LanguageEntity preferredLanguage;

    @Builder
    public MemberEntity(String email, LocalDate dateOfBirth, String password, String nickname, Integer gender) {
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.active = true;
        this.questionnaireCompleted = false;
        this.role = "ROLE_USER";
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Additional getter for isActive to match the field name
    public boolean isActive() {
        return active;
    }
    
    // Setter for active
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // Getter for questionnaireCompleted
    public boolean isQuestionnaireCompleted() {
        return questionnaireCompleted;
    }
    
    // Setter for questionnaireCompleted
    public void setQuestionnaireCompleted(boolean questionnaireCompleted) {
        this.questionnaireCompleted = questionnaireCompleted;
    }

    public void setLanguage(Integer languageId) {
        if (this.preferredLanguage == null) {
            this.preferredLanguage = new LanguageEntity();
        }
        this.preferredLanguage.setLanguageId(languageId);
    }
    
    public void setPreferredLanguage(LanguageEntity language) {
        this.preferredLanguage = language;
    }
}
