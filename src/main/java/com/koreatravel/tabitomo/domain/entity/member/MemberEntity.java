package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEntity {

    @Id
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email; // 이메일

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false)
    private String nickname; // 닉네임

    @Column
    private int gender;

    @Column(name = "country_id")
    private int countryId;

    @Column
    private boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column
    private int age;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
