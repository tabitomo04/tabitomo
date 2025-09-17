package com.koreatravel.tabitomo.repository.storybook;

import com.koreatravel.tabitomo.domain.entity.storybook.LikedbookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikedbookRepository extends JpaRepository<LikedbookEntity, Integer> {

    @Query("SELECT l FROM LikedbookEntity l WHERE l.booknum = :booknum AND l.member.id.id = :memberId")
    Optional<LikedbookEntity> findByBooknumAndMemberId(@Param("booknum") Integer booknum, @Param("memberId") Long memberId);
    
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LikedbookEntity l WHERE l.booknum = :booknum AND l.member.id.id = :memberId")
    boolean existsByBooknumAndMemberId(@Param("booknum") Integer booknum, @Param("memberId") Long memberId);
    
    @Modifying
    @Query("DELETE FROM LikedbookEntity l WHERE l.booknum = :booknum AND l.member.id.id = :memberId")
    void deleteByBooknumAndMemberId(@Param("booknum") Integer booknum, @Param("memberId") Long memberId);

}
