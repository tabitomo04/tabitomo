package com.koreatravel.tabitomo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
@Table(name = "member")
@EntityListeners(AuditingEntityListener.class)
public class MemberEntity {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(unique = true)
    private String email;

    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Column(name = "gender", nullable = false)
    private byte gender;

    @Column(name = "nickname", length = 45, nullable = false)
    private String nickname;

    @Column(name = "country_id", nullable = false)
    private int countryId;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT")
    private boolean isActive;

    @Column(name = "role", length = 20, nullable = false)
    private String role;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updateDate;
}
