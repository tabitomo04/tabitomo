package com.koreatravel.tabitomo.repository.storybook;

import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StorybookRepository extends JpaRepository<StorybookEntity, Integer> {
    @Query(value = "SELECT " +
            "    s.book_num AS booknum, " +
            "    s.title AS title, " +
            "    s.subtitle AS subtitle, " +
            "    s.likes AS likes, " +
            "    s.created_at AS createDate, " +
            "    (SELECT m.media_url " +
            "     FROM media m " +
            "     WHERE m.book_num = s.book_num AND m.media_type = 'image' " +
            "     ORDER BY m.uploaded_at ASC " +
            "     LIMIT 1) AS thumbnail " +
            "FROM storybook s " +
            "ORDER BY s.book_num DESC", // 최신 글 순으로 정렬
            nativeQuery = true)
    List<StorybookListDTO> StorybookList();

}
