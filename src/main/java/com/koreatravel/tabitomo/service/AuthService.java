package com.koreatravel.tabitomo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.koreatravel.tabitomo.domain.dto.MemberDTO;
import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import com.koreatravel.tabitomo.repository.MemberRepository;

@Service
public class AuthService {
    
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public AuthService(PasswordEncoder passwordEncoder, MemberRepository memberRepository) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Transactional
    public void registerNewMember(MemberDTO memberDTO) {
        if (memberRepository.existsByEmail(memberDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        MemberEntity member = MemberEntity.builder()
                .email(memberDTO.getEmail())
                .password(passwordEncoder.encode(memberDTO.getPassword()))
                .name(memberDTO.getName())
                .role("ROLE_USER")
                .build();

        memberRepository.save(member);
    }
}
