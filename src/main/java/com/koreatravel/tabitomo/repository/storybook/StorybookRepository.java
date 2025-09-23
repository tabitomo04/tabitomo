package com.koreatravel.tabitomo.repository.storybook;

import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StorybookRepository extends JpaRepository<StorybookEntity, Integer> {
    
    // 마이페이지의 스토리북 리스트 쿼리 (최신순)
    @Query(value = "SELECT " +
            "    s.book_num AS booknum, " +
            "    s.title AS title, " +
            "    s.subtitle AS subtitle, " +
            "    s.likes AS likes, " +
            "    s.created_at AS createDate, " +
            "    (SELECT md.media_url " +
            "     FROM media md " +
            "     WHERE md.num = s.book_num AND md.media_type = 'image' AND md.status = 'upload'" +
            "     ORDER BY md.uploaded_at ASC " +
            "     LIMIT 1) AS thumbnail, " +
            "    m.nickname AS nickname " +
            "FROM storybook s " +
            "JOIN member m ON s.member_id = m.id " +
            "WHERE m.id = :memberId " +
            "ORDER BY s.created_at DESC",
            nativeQuery = true)
    List<StorybookListDTO> StorybookList(@Param("memberId") UUID memberId);

    // 스토리북 리스트 페이지의 랜덤 리스트 쿼리
    @Query(value = "SELECT " +
            "    s.book_num AS booknum, " +
            "    s.title AS title, " +
            "    s.subtitle AS subtitle, " +
            "    s.likes AS likes, " +
            "    s.created_at AS createDate, " +
            "    (SELECT md.media_url " +
            "     FROM media md " +
            "     WHERE md.num = s.book_num AND md.media_type = 'image' AND md.status = 'upload'" +
            "     ORDER BY md.uploaded_at ASC " +
            "     LIMIT 1) AS thumbnail, " +
            "    m.nickname AS nickname " +
            "FROM storybook s " +
            "JOIN member m ON s.member_id = m.id " +
            "ORDER BY RAND() LIMIT 6",
            nativeQuery = true)
    List<StorybookListDTO> findListRandom();

    // 스토리북 리스트의 HOT, NEW 페이징 리스트 쿼리
    @Query(value = "SELECT " +
            "    s.book_num AS booknum, " +
            "    s.title AS title, " +
            "    s.subtitle AS subtitle, " +
            "    s.likes AS likes, " +
            "    s.created_at AS createDate, " +
            "    (SELECT md.media_url " +
            "     FROM media md " +
            "     WHERE md.num = s.book_num AND md.media_type = 'image' AND md.status = 'upload'" +
            "     ORDER BY md.uploaded_at ASC " +
            "     LIMIT 1) AS thumbnail, " +
            "    m.nickname AS nickname " +
            "FROM storybook s " +
            "JOIN member m ON s.member_id = m.id " +
            "ORDER BY " +
            "    CASE WHEN :sort = 'hot' THEN s.likes END DESC, " +
            "    CASE WHEN :sort = 'new' THEN s.created_at END DESC",
            countQuery = "SELECT COUNT(*) FROM storybook s",
            nativeQuery = true)
    Page<StorybookListDTO> hotORnewPage(@Param("sort") String sort, Pageable pageable);

    // 검색
    @Query(value = "SELECT " +
            "    s.book_num AS booknum, " +
            "    s.title AS title, " +
            "    s.subtitle AS subtitle, " +
            "    s.likes AS likes, " +
            "    s.created_at AS createDate, " +
            "    (SELECT md.media_url " +
            "     FROM media md " +
            "     WHERE md.num = s.book_num AND md.media_type = 'image' AND md.status = 'upload'" + // m.게시물번호_컬럼 = s.고유번호_컬럼
            "     ORDER BY md.uploaded_at ASC " + // m.업로드시간_컬럼
            "     LIMIT 1) AS thumbnail, " +
            "     m.nickname AS nickname " +
            "FROM storybook s " +
            "JOIN story_tag st ON s.book_num = st.booknum " +
            "JOIN tag_master tm ON st.tag_id = tm.tag_id " +
            "JOIN member m ON s.member_id = m.id " +
            "WHERE tm.tag_name LIKE CONCAT('%', :keyword, '%') " +
            "ORDER BY s.created_at DESC",
            nativeQuery = true)
    Page<StorybookListDTO> findbykeyword(@Param("keyword") String keyword, Pageable pageable);

}