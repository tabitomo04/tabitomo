package com.koreatravel.tabitomo.domain.dto.member;

import java.util.List;
import java.util.UUID;

import com.koreatravel.tabitomo.domain.dto.storybook.LikedbookDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.TempsaveDTO;
import com.koreatravel.tabitomo.domain.dto.trip.TripPlan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileDTO {
    private UUID id;
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