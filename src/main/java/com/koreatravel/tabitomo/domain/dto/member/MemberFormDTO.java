package com.koreatravel.tabitomo.domain.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberFormDTO {
    private int infohighnum;
    private int infolownum;
    private String content;
}
