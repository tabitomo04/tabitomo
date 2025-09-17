package com.koreatravel.tabitomo.domain.dto.storybook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LikedbookDTO {

    private Integer likeId;
    private Integer booknum;
    private MemberProfileDTO member;
    private LocalDateTime createDate;
}

