package com.koreatravel.tabitomo.domain.dto.trip;

import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.UserSelectedInfoEntity;
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

    public AddInfoEntity toAddInfoEntity() {
        return AddInfoEntity.builder()
                .infohighnum(infohighnum)
                .infolownum(infolownum)
                .content(content)
                .build();
    }

    public UserSelectedInfoEntity toUserSelectedInfoEntity() {
        return UserSelectedInfoEntity.builder()
                .infohighnum(infohighnum)
                .infolownum(infolownum)
                .build();
    }

    public static MemberFormDTO fromAddInfoEntity(AddInfoEntity addInfo) {
        return MemberFormDTO.builder()
                .infohighnum(addInfo.getInfohighnum())
                .infolownum(addInfo.getInfolownum())
                .content(addInfo.getContent())
                .build();
    }

    public static MemberFormDTO fromUserSelectedInfoEntity(UserSelectedInfoEntity userSelectedInfo) {
        return MemberFormDTO.builder()
                .infohighnum(userSelectedInfo.getInfohighnum())
                .infolownum(userSelectedInfo.getInfolownum())
                .build();
    }


}
