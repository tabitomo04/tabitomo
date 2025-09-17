package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class MemberEntity {
    
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

    @EmbeddedId
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private MemberId id;

    @Email
    @NotBlank
    @Size(max = 80)
    private String email;

    @NotBlank
    @Past
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "age")
    private Integer age;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, unique = true, length = 45)
    private String nickname;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean active = true;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "questionnaire_completed")
    private boolean questionnaireCompleted;

    @Column(name = "role")
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", referencedColumnName = "country_id")
    private CountryEntity country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_language_id")
    private LanguageEntity preferredLanguage;

    /**
     * countryId로 CountryEntity를 설정하는 편의 메서드
     */
    public void setCountry(int countryId) {
        if (this.country == null) {
            this.country = new CountryEntity();
        }
        this.country.setCountryId(countryId);
    }

    /**
     * languageId로 LanguageEntity를 설정하는 편의 메서드
     */
    public void setLanguage(int languageId) {
        if (this.preferredLanguage == null) {
            this.preferredLanguage = new LanguageEntity();
        }
        this.preferredLanguage.setLanguageId(languageId);
    }
}
