package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "member",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "nickname")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

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
    private boolean questionnaireCompleted = false;

    @Column(name = "role")
    private String role = "ROLE_USER";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", referencedColumnName = "country_id")
    private CountryEntity country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_language_id", referencedColumnName = "language_id")
    private LanguageEntity preferredLanguage;

    public void setCountry(CountryEntity country) {
        this.country = country;
    }

    public void setLanguage(LanguageEntity language) {
        this.preferredLanguage = language;
    }

    public void setCountry(int countryId) {
        this.country.setCountryId(countryId);
    }

    public void setLanguage(int languageId) {
        this.preferredLanguage.setLanguageId(languageId);
    }
}
