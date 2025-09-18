package com.koreatravel.tabitomo.config.security;

import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.repository.member.MemberAddInfoRepository;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberAddInfoRepository memberAddInfoRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException, DisabledException {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        // Check if account is active
        if (!member.isActive()) {
            throw new DisabledException("비활성화된 계정입니다. 관리자에게 문의해주세요.");
        }
        
        // Fetch additional member information
        List<MemberAddInfoEntity> additionalInfo = memberAddInfoRepository.findByMemberId(member.getId());
        
        return new UserDetailsImpl(member, additionalInfo);
    }
}
