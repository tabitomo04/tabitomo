package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.id.AddInfoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddInfoRepository extends JpaRepository<AddInfoEntity, AddInfoId> {
    
    /**
     * 특정 infoHighNum에 해당하는 모든 추가 정보를 조회합니다.
     * @param infoHighNum 조회할 infoHighNum
     * @return 조회된 추가 정보 목록
     */
    List<AddInfoEntity> findByInfoHighNum(Integer infoHighNum);
    
    /**
     * infoName으로 그룹화된 모든 추가 정보를 조회합니다.
     * @param infoName 조회할 infoName
     * @return 조회된 추가 정보 목록
     */
    List<AddInfoEntity> findByInfoName(String infoName);
    
    /**
     * infoHighNum과 infoName으로 추가 정보를 조회합니다.
     * @param infoHighNum 조회할 infoHighNum
     * @param infoName 조회할 infoName
     * @return 조회된 추가 정보 목록
     */
    List<AddInfoEntity> findByInfoHighNumAndInfoName(Integer infoHighNum, String infoName);
    
    /**
     * infoHighNum과 infoLowNum으로 추가 정보를 조회합니다.
     * @param infoHighNum 조회할 infoHighNum
     * @param infoLowNum 조회할 infoLowNum
     * @return 조회된 추가 정보 (Optional)
     */
    Optional<AddInfoEntity> findByInfoHighNumAndInfoLowNum(Integer infoHighNum, Long infoLowNum);
    
    /**
     * infoHighNum과 content로 추가 정보를 조회합니다.
     * @param infoHighNum 조회할 infoHighNum
     * @param content 조회할 content
     * @return 조회된 추가 정보 (Optional)
     */
    Optional<AddInfoEntity> findByInfoHighNumAndContent(Integer infoHighNum, String content);
}
