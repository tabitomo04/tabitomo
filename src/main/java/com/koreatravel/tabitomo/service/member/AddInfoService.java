package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import com.koreatravel.tabitomo.domain.dto.member.AddInfoDTO;
import com.koreatravel.tabitomo.repository.member.AddInfoRepository;
import com.koreatravel.tabitomo.repository.member.MemberAddInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddInfoService {

    private final AddInfoRepository addInfoRepository;
    private final MemberAddInfoRepository memberAddInfoRepository;

    @Transactional(readOnly = true)
    public Map<Integer, List<AddInfoDTO>> getAllAddInfoGrouped() {
        List<AddInfoEntity> allInfo = addInfoRepository.findAll();
        
        return allInfo.stream()
                .map(AddInfoDTO::fromEntity)
                .collect(Collectors.groupingBy(AddInfoDTO::getInfoHighNum));
    }

    @Transactional(readOnly = true)
    public List<AddInfoDTO> getAddInfoByType(Integer infoHighNum) {
        return addInfoRepository.findByInfoHighNum(infoHighNum).stream()
                .map(AddInfoDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void saveMemberAddInfo(UUID memberId, int infoHighNum, int infoLowNum) {
        MemberAddInfoEntity memberAddInfo = MemberAddInfoEntity.builder()
                .memberId(memberId)
                .addInfo(addInfoRepository.findByInfoHighNumAndInfoLowNum(infoHighNum, infoLowNum))
                .build();
                
        memberAddInfoRepository.save(memberAddInfo);
    }
}
