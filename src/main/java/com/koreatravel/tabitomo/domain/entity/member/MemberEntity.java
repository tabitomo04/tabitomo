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
    @Column(name = "email", nullable = false, unique = true)
    private String email; // 이메일

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false)
    private String nickname; // 닉네임

    @Column
    private int gender;

    @Column
    private int country_id;

    @Column
    private boolean isActive;

    @Column
    private LocalDateTime created_at;

    @Column
    private int age;

    @Column
    private LocalDateTime updated_at;
}
