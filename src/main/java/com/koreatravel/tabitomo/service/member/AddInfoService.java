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

    /**
     * 사용자가 이미 설문을 완료했는지 확인합니다.
     * @param memberId 확인할 사용자 ID
     * @return 설문 완료 여부
     */
    public boolean hasUserCompletedQuestionnaire(UUID memberId) {
        log.debug("Checking if user {} has completed the questionnaire", memberId);
        return memberAddInfoRepository.existsByMemberId(memberId);
    }

    /**
     * 사용자의 설문 완료 상태를 업데이트합니다.
     * @param memberId 사용자 ID
     * @param completed 완료 여부
     */
    @Transactional
    public void updateQuestionnaireCompletion(UUID memberId, boolean completed) {
        memberRepository.findById(memberId).ifPresent(member -> {
            member.setQuestionnaireCompleted(completed);
            memberRepository.save(member);
            log.info("Updated questionnaire completion status for member {} to {}", memberId, completed);
        });
    }

    @Transactional
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
            log.info("Updating questionnaire completion status for member: {}", memberId);
            Optional<MemberEntity> memberOpt = memberRepository.findById(memberId);
            if (memberOpt.isPresent()) {
                MemberEntity member = memberOpt.get();
                log.info("Current questionnaire status before update: {}", member.isQuestionnaireCompleted());
                member.setQuestionnaireCompleted(true);
                MemberEntity savedMember = memberRepository.save(member);
                log.info("Updated questionnaire completion status for member: {}, new status: {}", 
                        memberId, savedMember.isQuestionnaireCompleted());
            } else {
                log.error("Member not found with ID: {}", memberId);
            }
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
        
        try {
            // Try to find existing MBTI
            int mbtiId = Integer.parseInt(mbti);
            
            // Verify the MBTI exists in add_info table
            Optional<AddInfoEntity> existingMbti = addInfoRepository.findByInfoHighNumAndInfoLowNum(2, mbtiId);
            if (existingMbti.isEmpty()) {
                log.error("Invalid MBTI ID provided: {}", mbtiId);
                throw new BusinessException("유효하지 않은 MBTI 값입니다.");
            }
            
            // Save the reference in member_add_info
            saveMemberAddInfo(memberId, 2, mbtiId);
            log.info("Successfully saved MBTI for member: {}", memberId);
            
        } catch (NumberFormatException e) {
            log.error("Invalid MBTI format: {}", mbti, e);
            throw new BusinessException("MBTI 형식이 올바르지 않습니다.");
        }
    }
    
    // Internal method without @Transactional
    private void saveCategoryInternal(UUID memberId, int category, List<Long> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        // Delete existing items for this category
        memberAddInfoRepository.deleteByMemberIdAndInfoHighNum(memberId, category);
        
        // Save new items
        for (Long itemId : items) {
            // Verify the item exists in add_info table
            Optional<AddInfoEntity> existingItem = addInfoRepository.findByInfoHighNumAndInfoLowNum(category, itemId.intValue());
            if (existingItem.isPresent()) {
                saveMemberAddInfo(memberId, category, itemId.intValue());
            } else {
                log.warn("Invalid item ID {} for category {} provided by member {}", itemId, category, memberId);
            }
        }
    }
    
    // Save member add info
    private void saveMemberAddInfo(UUID memberId, int infoHighNum, int infoLowNum) {
        MemberAddInfoEntity entity = new MemberAddInfoEntity();
        entity.setMemberId(memberId);
        entity.setInfoHighNum(infoHighNum);
        entity.setInfoLowNum(infoLowNum);
        memberAddInfoRepository.save(entity);
    }
}