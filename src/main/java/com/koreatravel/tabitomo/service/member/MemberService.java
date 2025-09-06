package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.dto.member.MemberDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    public MemberEntity register(MemberEntity memberEntity) {
        log.info("회원가입 시도: {}", memberEntity.getEmail());
        
        // 이메일 중복 체크
        if (memberRepository.findByEmail(memberEntity.getEmail()).isPresent()) {
            log.warn("이미 가입된 이메일: {}", memberEntity.getEmail());
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
        
        // 닉네임 중복 체크 (성능을 위해 모든 회원을 조회하는 대신에 JPA 쿼리 메서드 사용이 더 나을 수 있음)
        memberRepository.findByNickname(memberEntity.getNickname())
            .ifPresent(m -> {
                log.warn("이미 사용 중인 닉네임: {}", memberEntity.getNickname());
                throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
            });
        
        // 비밀번호 암호화
        memberEntity.setPassword(passwordEncoder.encode(memberEntity.getPassword()));
        memberEntity.setActive(true);
        
        return memberRepository.save(memberEntity);
    }

    /**
     * 로그인 처리 (Spring Security를 사용하므로 실제로는 Security에서 처리)
     * @deprecated Use Spring Security's authentication manager instead
     */
    @Deprecated
    public boolean login(String email, String rawPassword) {
        return memberRepository.findByEmail(email)
                .map(member -> passwordEncoder.matches(rawPassword, member.getPassword()))
                .orElse(false);
    }
    
    /**
     * 현재 인증된 사용자 정보 조회
     */
    public Optional<MemberEntity> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("인증된 사용자가 없습니다.");
            return Optional.empty();
        }
        
        String email = authentication.getName();
        log.debug("현재 인증된 사용자 조회: {}", email);
        return memberRepository.findByEmail(email);
    }
    
    /**
     * 이메일로 사용자 조회
     */
    public Optional<MemberEntity> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
}

