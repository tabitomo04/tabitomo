package com.koreatravel.tabitomo.service;

import com.koreatravel.tabitomo.domain.dto.MemberDTO;
import com.koreatravel.tabitomo.domain.dto.MemberFormDTO;
import com.koreatravel.tabitomo.domain.entity.AddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.UserSelectedInfoEntity;
import com.koreatravel.tabitomo.repository.AddInfoRepository;
import com.koreatravel.tabitomo.repository.MemberRepository;
import com.koreatravel.tabitomo.repository.UserSelectedInfoRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserSelectedInfoRepository userSelectedInfoRepository;
    private final AddInfoRepository addInfoRepository;

    public MemberDTO getMemberByEmail(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return MemberDTO.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .countryId(member.getCountryId())
                .isActive(member.isActive())
                .build();
    }

    public void saveUserSelectedInfo(String email, List<MemberFormDTO> memberFormDTO) {
        for (MemberFormDTO memberForm : memberFormDTO) {
            UserSelectedInfoEntity userSelectedInfo = UserSelectedInfoEntity.builder()
                    .infohighnum(memberForm.getHighnum())
                    .infolownum(memberForm.getLownum())
                    .email(email)
                    .build();
            userSelectedInfoRepository.save(userSelectedInfo);
        }
    }

    public List<AddInfoEntity> getAddInfoList() {
        return addInfoRepository.findAll();
    }
}
