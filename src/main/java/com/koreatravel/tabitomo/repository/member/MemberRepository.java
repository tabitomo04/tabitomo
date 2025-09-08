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
    
    Optional<MemberEntity> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByNickname(String nickname);
    
    // Active user queries
    @Query("SELECT m FROM MemberEntity m WHERE m.email = :email AND m.isActive = true")
    Optional<MemberEntity> findActiveByEmail(@Param("email") String email);
    
    @Query("SELECT m FROM MemberEntity m WHERE m.nickname = :nickname")
    Optional<MemberEntity> findByNickname(@Param("nickname") String nickname);
    
    // Account management
    @Modifying
    @Query("UPDATE MemberEntity m SET m.isActive = false WHERE m.email = :email")
    int deactivateByEmail(@Param("email") String email);
    
    // Email verification
    @Modifying
    @Query("UPDATE MemberEntity m SET m.emailVerified = true, m.emailVerifyToken = null WHERE m.email = :email")
    int verifyEmail(@Param("email") String email);
    
    @Query("SELECT m FROM MemberEntity m WHERE m.emailVerifyToken = :token")
    Optional<MemberEntity> findByEmailVerifyToken(@Param("token") String token);
    
    // Password reset
    @Modifying
    @Query("UPDATE MemberEntity m SET m.password = :password, m.passwordResetToken = null, m.passwordResetExpires = null WHERE m.email = :email")
    int updatePassword(@Param("email") String email, @Param("password") String password);
    
    @Modifying
    @Query("UPDATE MemberEntity m SET m.passwordResetToken = :token, m.passwordResetExpires = :expiryDate WHERE m.email = :email")
    int setPasswordResetToken(
        @Param("email") String email, 
        @Param("token") String token, 
        @Param("expiryDate") LocalDateTime expiryDate
    );
    
    @Query("SELECT m FROM MemberEntity m WHERE m.passwordResetToken = :token AND m.passwordResetExpires > :now")
    Optional<MemberEntity> findByPasswordResetToken(
        @Param("token") String token,
        @Param("now") LocalDateTime now
    );
    
    // Profile updates
    @Modifying
    @Query("UPDATE MemberEntity m SET m.nickname = :nickname WHERE m.email = :email")
    int updateNickname(@Param("email") String email, @Param("nickname") String nickname);
    
    @Modifying
    @Query("UPDATE MemberEntity m SET m.profileImageUrl = :imageUrl WHERE m.email = :email")
    int updateProfileImage(@Param("email") String email, @Param("imageUrl") String imageUrl);
}
