package com.koreatravel.tabitomo.service;

import com.koreatravel.tabitomo.domain.dto.MemberDTO;
import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import com.koreatravel.tabitomo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().replace("ROLE_", ""))
                .build();
    }
}
