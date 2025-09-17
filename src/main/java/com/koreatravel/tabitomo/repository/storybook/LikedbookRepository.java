package com.koreatravel.tabitomo.repository.storybook;

import com.koreatravel.tabitomo.domain.entity.storybook.LikedbookEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedbookRepository extends JpaRepository<LikedbookEntity, Integer> {

    boolean existsByStorybookAndMember(StorybookEntity storybook, MemberEntity member);

    void deleteByStorybookAndMember(StorybookEntity storybook, MemberEntity member);

}
