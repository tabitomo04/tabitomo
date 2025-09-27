package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.dto.member.AddInfoDTO;
import com.koreatravel.tabitomo.domain.dto.member.QuestionAnswersDTO;
import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.exception.BusinessException;
import com.koreatravel.tabitomo.repository.member.AddInfoRepository;
import com.koreatravel.tabitomo.repository.member.MemberAddInfoRepository;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddInfoService {
    private final MemberRepository memberRepository;
    private final AddInfoRepository addInfoRepository;
    private final MemberAddInfoRepository memberAddInfoRepository;

    /**
     * 카테고리별 질문 목록을 조회합니다.
     * @param categories 조회할 카테고리 ID 목록 (info_high_num)
     * @return 카테고리 ID를 키로, 해당 카테고리의 질문 목록을 값으로 가지는 Map
     */
    public Map<Integer, List<AddInfoDTO>> getQuestionsByCategories(List<Integer> categories) {
        log.info("Fetching questions for categories: {}", categories);
        Map<Integer, List<AddInfoDTO>> result = new HashMap<>();
        
        for (Integer category : categories) {
            List<AddInfoEntity> entities = addInfoRepository.findByInfoHighNum(category);
            List<AddInfoDTO> dtos = entities.stream()
                    .map(AddInfoDTO::fromEntity)
                    .collect(Collectors.toList());
            result.put(category, dtos);
        }
        
        return result;
    }

    public void saveAnswers(UUID memberId, QuestionAnswersDTO answers) {
        log.info("Saving answers for member: {}", memberId);
        
        try {
            // Save MBTI if provided
            if (answers.getMbti() != null && !answers.getMbti().trim().isEmpty()) {
                saveMbtiInternal(memberId, answers.getMbti());
            } else if (answers.getMbtiId() != null) {
                saveMbtiInternal(memberId, answers.getMbtiId().toString());
            }
            
            // Save other categories
            saveCategoryInternal(memberId, 1, answers.getHobbies());
            saveCategoryInternal(memberId, 3, answers.getTravelStyles());
            saveCategoryInternal(memberId, 4, answers.getCompanions());
            saveCategoryInternal(memberId, 5, answers.getFoodPreferences());
            
            // Update questionnaire completion status
            memberRepository.findById(memberId).ifPresent(member -> {
                member.setQuestionnaireCompleted(true);
                memberRepository.save(member);
                log.info("Updated questionnaire completion status for member: {}", memberId);
            });
            
        } catch (Exception e) {
            log.error("Error saving answers for member {}: {}", memberId, e.getMessage(), e);
            throw new BusinessException("Failed to save answers: " + e.getMessage());
        }
    }
    
    // Internal method without @Transactional
    private void saveMbtiInternal(UUID memberId, String mbti) {
        if (mbti == null || mbti.trim().isEmpty()) {
            log.warn("MBTI is null or empty for member: {}", memberId);
            return;
        }
        
        // Delete existing MBTI
        memberAddInfoRepository.deleteByMemberIdAndInfoHighNum(memberId, 2);
        
        // Save new MBTI
        int mbtiId = getMbtiId(mbti);
        saveMemberAddInfo(memberId, 2, mbtiId);
        
        log.info("Successfully saved MBTI for member: {}", memberId);
    }
    
    // Internal method without @Transactional
    private void saveCategoryInternal(UUID memberId, int category, List<Long> items) {
        if (items == null || items.isEmpty()) {
            log.warn("No items provided for category: {}", category);
            return;
        }
        
        // Delete existing entries for this category
        memberAddInfoRepository.deleteByMemberIdAndInfoHighNum(memberId, category);
        
        // Save new items
        for (Long itemId : items) {
            try {
                saveMemberAddInfo(memberId, category, itemId.intValue());
            } catch (Exception e) {
                log.error("Error saving item {} for member: {}, category: {}", 
                        itemId, memberId, category, e);
                throw new BusinessException("Failed to save category " + category + ": " + e.getMessage());
            }
        }
    }

    public List<MemberAddInfoEntity> getMemberAddInfo(UUID memberId) {
        log.info("Fetching additional info for member: {}", memberId);
        return memberAddInfoRepository.findByMemberId(memberId);
    }

    @Transactional
    public void deleteMemberAddInfo(UUID memberId, int infoHighNum) {
        log.info("Deleting additional info for member: {}, category: {}", memberId, infoHighNum);
        memberAddInfoRepository.deleteByMemberIdAndInfoHighNum(memberId, infoHighNum);
    }

    public boolean hasMemberAddInfo(UUID memberId) {
        return memberAddInfoRepository.existsByMemberId(memberId);
    }

    @Transactional
    public void saveOrUpdateMemberAddInfo(UUID memberId, int infoHighNum, int infoLowNum) {
        if (memberAddInfoRepository.existsByMemberIdAndInfoHighNumAndInfoLowNum(
                memberId, infoHighNum, infoLowNum)) {
            log.info("Member add info already exists - memberId: {}, infoHighNum: {}, infoLowNum: {}", 
                    memberId, infoHighNum, infoLowNum);
            return;
        }
        saveMemberAddInfo(memberId, infoHighNum, infoLowNum);
    }

    @Transactional
    public void saveNewMemberAddInfo(UUID memberId, int infoHighNum, int infoLowNum) {
        saveMemberAddInfo(memberId, infoHighNum, infoLowNum);
    }

    public void updateMemberAddInfo(UUID memberId, int infoHighNum, int infoLowNum) {
        try {
            deleteMemberAddInfo(memberId, infoHighNum);
            saveMemberAddInfo(memberId, infoHighNum, infoLowNum);
        } catch (Exception e) {
            log.error("Error updating member add info - memberId: {}, infoHighNum: {}, infoLowNum: {}: {}", 
                    memberId, infoHighNum, infoLowNum, e.getMessage(), e);
            throw new BusinessException("Failed to update member add info: " + e.getMessage());
        }
    }

    private void saveMemberAddInfo(UUID memberId, int infoHighNum, int infoLowNum) {
        log.info("Saving member add info - memberId: {}, infoHighNum: {}, infoLowNum: {}", 
                memberId, infoHighNum, infoLowNum);
        
        try {
            // Check if the record already exists
            if (memberAddInfoRepository.existsByMemberIdAndInfoHighNumAndInfoLowNum(
                    memberId, infoHighNum, infoLowNum)) {
                log.info("Member add info already exists - memberId: {}, infoHighNum: {}, infoLowNum: {}", 
                        memberId, infoHighNum, infoLowNum);
                return;
            }
            
            // Find or create the corresponding AddInfoEntity
            AddInfoEntity addInfo = addInfoRepository
                .findByInfoHighNumAndInfoLowNum(infoHighNum, infoLowNum)
                .orElseGet(() -> {
                    // Create a new AddInfoEntity if it doesn't exist
                    AddInfoEntity newAddInfo = AddInfoEntity.builder()
                        .infoHighNum(infoHighNum)
                        .infoLowNum(infoLowNum)
                        .infoName("Auto-generated " + infoHighNum + "-" + infoLowNum)
                        .content("Automatically generated entry")
                        .build();
                    return addInfoRepository.save(newAddInfo);
                });
            
            // Create and save the new entity
            MemberAddInfoEntity entity = MemberAddInfoEntity.builder()
                    .memberId(memberId)
                    .infoHighNum(infoHighNum)
                    .infoLowNum(infoLowNum)
                    .addInfo(addInfo)
                    .build();
            
            memberAddInfoRepository.save(entity);
            log.info("Successfully saved member add info - memberId: {}, infoHighNum: {}, infoLowNum: {}", 
                    memberId, infoHighNum, infoLowNum);
            
        } catch (Exception e) {
            log.error("Error saving member add info - memberId: {}, infoHighNum: {}, infoLowNum: {}: {}", 
                    memberId, infoHighNum, infoLowNum, e.getMessage(), e);
            throw new BusinessException("Failed to save member add info: " + e.getMessage());
        }
    }
    
    @Deprecated
    public void saveCategory(UUID memberId, int category, List<Long> items) {
        saveCategoryInternal(memberId, category, items);
    }
    
    @Deprecated
    public void saveMbti(UUID memberId, String mbti) {
        saveMbtiInternal(memberId, mbti);
    }

    private int getMbtiId(String mbti) {
        // This is a simplified example - you should implement your own logic to map MBTI to IDs
        // For now, we'll use a simple hash of the MBTI string to generate a consistent ID
        return Math.abs(mbti.trim().toUpperCase().hashCode() % 100);
    }

    @Transactional
    protected void saveMemberAddInfoInNewTransaction(UUID memberId, int infoHighNum, int infoLowNum) {
        saveMemberAddInfo(memberId, infoHighNum, infoLowNum);
    }
}