package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.domain.entity.LikedbookEntity;
import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.StorybookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedbookRepository extends JpaRepository<LikedbookEntity, Integer> {

    boolean existsByStorybookAndMember(StorybookEntity storybook, MemberEntity member);

    void deleteByStorybookAndMember(StorybookEntity storybook, MemberEntity member);

}
