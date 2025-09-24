package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.auth.SignUpDTO;

public interface AuthServiceInterface {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    void signup(SignUpDTO dto, int countryId, int languageId);
    MemberProfileDTO login(String email, String password);
    MemberProfileDTO getMemberProfileByEmail(String email);
    void storeResetToken(String email, String token);
    boolean resetPassword(String email, String newPassword);
}
