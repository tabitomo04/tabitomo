package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    
    // Basic CRUD operations
    @Override
    Optional<MemberEntity> findById(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByNickname(String nickname);
    
    // Active user queries
    @Query("SELECT m FROM MemberEntity m WHERE m.email = :email AND m.isActive = true")
    Optional<MemberEntity> findActiveByEmail(@Param("email") String email);
    
    // Find by email (active or inactive)
    Optional<MemberEntity> findByEmail(String email);
    
    @Query("SELECT m FROM MemberEntity m WHERE m.nickname = :nickname")
    Optional<MemberEntity> findByNickname(@Param("nickname") String nickname);
    
    // Account management
    @Modifying
    @Query("UPDATE MemberEntity m SET m.isActive = false WHERE m.email = :email")
    int deactivateByEmail(@Param("email") String email);
    
    // Password update
    @Modifying
    @Query("UPDATE MemberEntity m SET m.password = :password WHERE m.email = :email")
    int updatePassword(@Param("email") String email, @Param("password") String password);
    
    // Find by username (alias for findByEmail to match 박세훈/tabitomo)
    default Optional<MemberEntity> findByUsername(String username) {
        return findByEmail(username);
    }
    
    // Profile updates
    @Modifying
    @Query("UPDATE MemberEntity m SET m.nickname = :nickname WHERE m.email = :email")
    int updateNickname(@Param("email") String email, @Param("nickname") String nickname);
    
    @Modifying
    @Query("UPDATE MemberEntity m SET m.profileImageUrl = :imageUrl WHERE m.email = :email")
    int updateProfileImage(@Param("email") String email, @Param("imageUrl") String imageUrl);
}
