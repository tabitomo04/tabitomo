package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.AddInfoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddInfoRepository extends JpaRepository<AddInfoEntity, AddInfoId> {
    List<AddInfoEntity> findByInfoHighNum(int infoHighNum);
    Optional<AddInfoEntity> findByInfoHighNumAndInfoLowNum(int infoHighNum, int infoLowNum);
    
    @Query("SELECT a FROM AddInfoEntity a WHERE a.id.infoHighNum = :infoHighNum AND a.infoName = :infoName")
    Optional<AddInfoEntity> findByInfoHighNumAndInfoName(
        @Param("infoHighNum") int infoHighNum, 
        @Param("infoName") String infoName
    );
}
