package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.repository.member.MemberRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 회원가입
    public MemberEntity register(MemberEntity memberEntity) {
        // 비밀번호 암호화
        memberEntity.setPassword(passwordEncoder.encode(memberEntity.getPassword()));
        return memberRepository.save(memberEntity);
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
}

