package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.dto.member.AddInfoDTO;
import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.repository.member.AddInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddInfoService {

    private final AddInfoRepository addInfoRepository;

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
    
    @Transactional(readOnly = true)
    public List<AddInfoDTO> getAddInfoByName(String infoName) {
        return addInfoRepository.findByInfoName(infoName).stream()
                .map(AddInfoDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
