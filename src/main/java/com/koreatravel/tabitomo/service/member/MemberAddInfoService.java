package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.id.MemberAddInfoId;
import com.koreatravel.tabitomo.repository.member.AddInfoRepository;
import com.koreatravel.tabitomo.repository.member.MemberAddInfoRepository;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing member additional information
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberAddInfoService {

    private final MemberAddInfoRepository memberAddInfoRepository;
    private final MemberRepository memberRepository;
    private final AddInfoRepository addInfoRepository;

    /**
     * Find all additional information for a member
     */
    public List<MemberAddInfoEntity> findByMemberId(UUID memberId) {
        return memberAddInfoRepository.findByMemberId(memberId);
    }

    /**
     * Find all additional information for a member by info high number
     */
    public List<MemberAddInfoEntity> findByMemberIdAndInfoHighNum(UUID memberId, Integer infoHighNum) {
        return memberAddInfoRepository.findByMemberIdAndAddInfoInfoHighNum(memberId, infoHighNum);
    }

    /**
     * Save member additional information
     */
    @Transactional
    public MemberAddInfoEntity save(MemberAddInfoEntity memberAddInfo) {
        return memberAddInfoRepository.save(memberAddInfo);
    }

    /**
     * Save multiple member additional information entries
     */
    @Transactional
    public List<MemberAddInfoEntity> saveAll(Iterable<MemberAddInfoEntity> memberAddInfos) {
        return memberAddInfoRepository.saveAll(memberAddInfos);
    }

    /**
     * Delete all additional information for a member
     */
    @Transactional
    public void deleteByMemberId(UUID memberId) {
        memberAddInfoRepository.deleteByMemberId(memberId);
    }

    /**
     * Delete a specific member additional information entry
     */
    @Transactional
    public void deleteById(MemberAddInfoId id) {
        memberAddInfoRepository.deleteById(id);
    }

    /**
     * Delete multiple member additional information entries
     */
    @Transactional
    public void deleteAll(Iterable<MemberAddInfoEntity> memberAddInfos) {
        memberAddInfoRepository.deleteAll(memberAddInfos);
    }
    
    /**
     * Create a new MemberAddInfoEntity
     */
    public MemberAddInfoEntity createMemberAddInfo(UUID memberId, Integer infoHighNum, Integer infoLowNum, String infoName) {
        // Check if the record already exists
        List<MemberAddInfoEntity> existingInfos = memberAddInfoRepository
            .findByMemberIdAndInfoHighNumAndInfoLowNum(memberId, infoHighNum, infoLowNum);
            
        if (!existingInfos.isEmpty()) {
            return existingInfos.get(0);
        }
        
        // Create new entity
        MemberEntity member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
            
        AddInfoEntity addInfo = addInfoRepository.findByInfoHighNumAndInfoLowNum(infoHighNum, infoLowNum)
            .orElseThrow(() -> new ResourceNotFoundException("AddInfo not found with highNum: " + infoHighNum + " and lowNum: " + infoLowNum));
        
        // Create the composite ID
        MemberAddInfoId id = new MemberAddInfoId(memberId, infoHighNum, infoLowNum);
        
        MemberAddInfoEntity memberAddInfo = MemberAddInfoEntity.builder()
            .id(id)
            .member(member)
            .addInfo(addInfo)
            .createdAt(LocalDateTime.now())
            .build();
            
        return memberAddInfoRepository.save(memberAddInfo);
    }
}
