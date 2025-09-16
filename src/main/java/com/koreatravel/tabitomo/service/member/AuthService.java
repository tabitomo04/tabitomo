package com.koreatravel.tabitomo.service.member;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import java.util.UUID;

import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.domain.dto.auth.SignUpDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;


@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 회원가입
    public void signup(SignUpDTO dto, int countryId, int languageId) {
        // 비밀번호 암호화
        MemberEntity memberEntity = MemberEntity.builder()
                .email(dto.getEmail())
                .dateOfBirth(dto.getBirthDate())
                
                .nickname(dto.getNickname())
                .build();
        memberEntity.setPassword(passwordEncoder.encode(memberEntity.getPassword()));
        memberEntity.setCountry(countryId);
        memberEntity.setLanguage(languageId);
        memberRepository.save(memberEntity);
    }

    // 로그인
    public boolean login(String email, String rawPassword) {
        Optional<MemberEntity> userOpt = memberRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            MemberEntity memberEntity = userOpt.get();
            return passwordEncoder.matches(rawPassword, memberEntity.getPassword());
        }
        return false;
    }

    public MemberEntity findById(UUID id) {
        return memberRepository.findById(id).orElse(null);
    }
}