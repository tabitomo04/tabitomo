package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.UserSelectedInfoEntity;
import com.koreatravel.tabitomo.id.UserSelectedInfoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSelectedInfoRepository extends JpaRepository<UserSelectedInfoEntity, UserSelectedInfoId> {
    List<UserSelectedInfoEntity> findByEmail(String email);
    void deleteByEmail(String email);
    
    default void deleteByEmailAndInfoHighNum(String email, Integer infoHighNum) {
        List<UserSelectedInfoEntity> toDelete = findByEmail(email).stream()
            .filter(entity -> entity.getInfoHighNum().equals(infoHighNum))
            .collect(java.util.stream.Collectors.toList());
        deleteAll(toDelete);
    }
}
