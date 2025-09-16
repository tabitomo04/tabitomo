package com.koreatravel.tabitomo.controller.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.koreatravel.tabitomo.service.member.MemberService;
import com.koreatravel.tabitomo.service.trip.TripPlanService;
import com.koreatravel.tabitomo.service.storybook.EditorService;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private TripPlanService tripService;

    @Autowired
    private EditorService storybookService;

    // 멤버 프로필(정보) -> 다른 사람도 표기되어야 함
    @GetMapping("/member/info/{memberId}")
    public String viewMemberProfile(@PathVariable String memberId) {
        return "member/profile";
    }

    // 멤버 프로필 수정
    @GetMapping("/member/profile/update")
    public String updateProfile() {
        return "member/update-profile";
    }

    // 멤버의 스토리북 목록
    @GetMapping("/member/storybook/list")
    public String viewStorybookList() {
        return "member/storybook-list";
    }

    // 멤버의 좋아하는 관광지 목록
    @GetMapping("/member/attraction/list")
    public String viewAttractionList() {
        return "member/attraction-list";
    }

    // 멤버의 likedbook 목록
    @GetMapping("/member/likedbook/list")
    public String viewLikedbookList() {
        return "member/likedbook-list";
    }

    // 멤버의 여행 목록
    @GetMapping("/member/trip/list")
    public String viewTripList() {
        return "member/trip-list";
    }
}
