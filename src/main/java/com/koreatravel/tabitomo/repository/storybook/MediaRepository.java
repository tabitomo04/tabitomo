package com.koreatravel.tabitomo.repository.storybook;

import org.springframework.data.jpa.repository.JpaRepository;
import com.koreatravel.tabitomo.domain.entity.storybook.MediaEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.MediaEntity.MediaStatus;
import java.util.List;

public interface MediaRepository extends JpaRepository<MediaEntity, Integer> {
    void deleteByBookNum(Integer bookNum);
    void deleteByBookNumAndStatus(Integer bookNum, MediaStatus status);
    List<MediaEntity> findByBookNumAndStatus(Integer bookNum, MediaStatus status);
}
