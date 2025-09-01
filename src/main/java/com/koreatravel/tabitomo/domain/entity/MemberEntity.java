package com.koreatravel.tabitomo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String nickname;

    private int countryId;

    @Column(nullable = false)
    private String role;

    @Builder.Default
    private boolean isActive = true;

    @Column
    private String status;

    @Column
    private String createdAt;

    @Column
    private String updatedAt;
}
