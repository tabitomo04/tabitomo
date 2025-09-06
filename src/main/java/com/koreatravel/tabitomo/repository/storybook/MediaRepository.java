package com.koreatravel.tabitomo.repository.storybook;

import org.springframework.data.jpa.repository.JpaRepository;

import com.koreatravel.tabitomo.domain.entity.storybook.MediaEntity;

import java.util.List;

public interface MediaRepository extends JpaRepository<MediaEntity, Integer> {
    void deleteByNum(Integer num);
    void deleteByNumAndStatus(Integer num, String status);

    List<MediaEntity> findByNumAndStatus(Integer num, String status);

}
