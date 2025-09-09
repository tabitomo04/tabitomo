package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.dto.member.AddInfoDto;
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
    public Map<Integer, List<AddInfoDto>> getAllAddInfoGrouped() {
        List<AddInfoEntity> allInfo = addInfoRepository.findAll();
        
        return allInfo.stream()
                .map(AddInfoDto::fromEntity)
                .collect(Collectors.groupingBy(AddInfoDto::getInfoHighNum));
    }

    @Transactional(readOnly = true)
    public List<AddInfoDto> getAddInfoByType(Integer infoHighNum) {
        return addInfoRepository.findByInfoHighNum(infoHighNum).stream()
                .map(AddInfoDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AddInfoDto> getAddInfoByName(String infoName) {
        return addInfoRepository.findByInfoName(infoName).stream()
                .map(AddInfoDto::fromEntity)
                .collect(Collectors.toList());
    }
}
