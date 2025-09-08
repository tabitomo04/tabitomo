package com.koreatravel.tabitomo.domain.entity.member;

import lombok.Getter;

/**
 * 회원 권한을 정의하는 열거형
 */
@Getter
public enum MemberRole {
    ROLE_USER("일반 사용자"),
    ROLE_ADMIN("관리자"),
    ROLE_GUEST("게스트");

    private final String description;

    MemberRole(String description) {
        this.description = description;
    }
}
