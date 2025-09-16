package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "member")
public class MemberEntity {
    
    @Id
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    
    @Column(name = "email", nullable = false, unique = true, length = 80)
    private String email;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "gender", length = 10)
    private String gender;
    
    @Column(name = "password", nullable = false, length = 100)
    private String password;
    
    @Column(name = "name", length = 50)
    private String name;
    
    @Column(name = "nickname", length = 50)
    private String nickname;
    
    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;
    
    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;
    
    @Column(name = "status", length = 20)
    private String status;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
