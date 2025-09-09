package com.koreatravel.tabitomo.config.security;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", email);
        
        return memberRepository.findById(email)
                .filter(MemberEntity::isActive)
                .map(MemberDetails::new)
                .orElseThrow(() -> {
                    log.warn("User not found or inactive: {}", email);
                    return new UsernameNotFoundException("사용자를 찾을 수 없거나 비활성화된 계정입니다: " + email);
                });
    }
}
