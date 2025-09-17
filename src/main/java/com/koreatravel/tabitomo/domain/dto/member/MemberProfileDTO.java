package com.koreatravel.tabitomo.domain.dto.member;

import java.util.List;

import com.koreatravel.tabitomo.domain.dto.storybook.LikedbookDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.TempsaveDTO;
import com.koreatravel.tabitomo.domain.dto.trip.TripPlan;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberProfileDTO {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String introduction;
    private String role;
    private CountryDTO country;
    private LanguageDTO language;

    private List<StorybookDTO> storybooks;
    private List<LikedbookDTO> likedbooks;
    private List<TempsaveDTO> tempsaves;
    private List<TripPlan> tripplans;
}