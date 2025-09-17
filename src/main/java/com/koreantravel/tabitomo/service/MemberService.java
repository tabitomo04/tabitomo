package com.koreantravel.tabitomo.service;

import com.koreantravel.tabitomo.domain.entity.Member;
import com.koreantravel.tabitomo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String join(Member member) {
        validateDuplicateMember(member);
        Member newMember = Member.builder()
                .email(member.getEmail())
                .password(passwordEncoder.encode(member.getPassword()))
                .gender(member.getGender())
                .nickname(member.getNickname())
                .countryId(member.getCountryId())
                .build();
        memberRepository.save(newMember);
        return newMember.getEmail();
    }

    private void validateDuplicateMember(Member member) {
        memberRepository.findById(member.getEmail())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 가입된 이메일입니다.");
                });
        memberRepository.findByNickname(member.getNickname())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 닉네임입니다.");
                });
    }

    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + email));
    }

    @Transactional
    public void updateMemberByAdmin(String email, String nickname, String role, boolean isActive) {
        Member member = findMemberByEmail(email);
        member.updateNickname(nickname);
        member.updateRole(role);
        member.updateIsActive(isActive);
    }

    @Transactional
    public void deleteMemberByAdmin(String email) {
        memberRepository.deleteById(email);
    }

    @Transactional
    public void toggleUserActiveState(String email) {
        Member member = findMemberByEmail(email);
        member.updateIsActive(!member.isActive());
    }
}
