package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import com.koreatravel.tabitomo.domain.dto.member.AddInfoDTO;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    
    @Transactional(readOnly = true)
    public AddInfoEntity getAddInfoByTypeAndName(int infoHighNum, String infoName) {
        log.debug("Looking up AddInfoEntity with infoHighNum: {}, infoName: {}", infoHighNum, infoName);
        List<AddInfoEntity> allOfType = addInfoRepository.findByInfoHighNum(infoHighNum);
        
        // Log all available MBTI values for debugging
        log.debug("Available values for type {}:", infoHighNum);
        allOfType.forEach(info -> log.debug("- {} (ID: {})", info.getInfoName(), info.getInfoLowNum()));
        
        // Find exact match first (case-sensitive)
        AddInfoEntity exactMatch = allOfType.stream()
                .filter(info -> info.getInfoName().equals(infoName))
                .findFirst()
                .orElse(null);
                
        if (exactMatch != null) {
            log.debug("Found exact match: {}", exactMatch.getInfoName());
            return exactMatch;
        }
        
        // If no exact match, try case-insensitive match
        AddInfoEntity caseInsensitiveMatch = allOfType.stream()
                .filter(info -> info.getInfoName().equalsIgnoreCase(infoName))
                .findFirst()
                .orElse(null);
                
        if (caseInsensitiveMatch != null) {
            log.debug("Found case-insensitive match: {}", caseInsensitiveMatch.getInfoName());
        } else {
            log.debug("No match found for: {}", infoName);
        }
        
        return caseInsensitiveMatch;
    }
    
    @Transactional
    public void saveMemberAddInfo(UUID memberId, int infoHighNum, int infoLowNum) {
        log.info("Saving member add info - memberId: {}, infoHighNum: {}, infoLowNum: {}", memberId, infoHighNum, infoLowNum);
        
        try {
            // Find the AddInfoEntity first
            AddInfoEntity addInfo = addInfoRepository.findByInfoHighNumAndInfoLowNum(infoHighNum, infoLowNum);
            
            if (addInfo == null) {
                log.error("AddInfoEntity not found for infoHighNum: {}, infoLowNum: {}", infoHighNum, infoLowNum);
                throw new IllegalArgumentException("Invalid infoHighNum or infoLowNum: " + infoHighNum + ", " + infoLowNum);
            }
            
            // Create and save the new entry
            MemberAddInfoEntity memberAddInfo = MemberAddInfoEntity.builder()
                    .memberId(memberId)
                    .addInfo(addInfo)
                    .build();
                    
            // This will handle the save operation and throw an exception if there's a duplicate key
            memberAddInfoRepository.save(memberAddInfo);
            log.info("Successfully saved member add info - memberId: {}, infoHighNum: {}, infoLowNum: {}", 
                    memberId, infoHighNum, infoLowNum);
                    
        } catch (Exception e) {
            log.error("Error saving member add info - memberId: {}, infoHighNum: {}, infoLowNum: {}", 
                    memberId, infoHighNum, infoLowNum, e);
            throw new RuntimeException("Failed to save member add info: " + e.getMessage(), e);
        }
    }
}
