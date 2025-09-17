package com.koreantravel.tabitomo.controller;

import com.koreantravel.tabitomo.domain.entity.Member;
import com.koreantravel.tabitomo.domain.entity.Trip;
import com.koreantravel.tabitomo.service.MemberService;
import com.koreantravel.tabitomo.service.TripPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('admin')")
@RequiredArgsConstructor
public class AdminController {

    private final MemberService memberService;
    private final TripPlanService tripPlanService;

    @GetMapping
    public String adminPage() {
        return "admin/index";
    }

    // 회원 관리
    @GetMapping("/users")
    public String userManagement(Model model) {
        List<Member> members = memberService.findAllMembers();
        model.addAttribute("members", members);
        return "admin/users";
    }

    @GetMapping("/users/edit/{email:.+}")
    public String showEditForm(@PathVariable("email") String email, Model model) {
        Member member = memberService.findMemberByEmail(email);
        model.addAttribute("member", member);
        return "admin/user-edit";
    }

    @PostMapping("/users/edit")
    public String updateUser(@ModelAttribute Member member) {
        memberService.updateMemberByAdmin(member.getEmail(), member.getNickname(), member.getRole(), member.isActive());
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{email:.+}")
    public String deleteUser(@PathVariable("email") String email) {
        memberService.deleteMemberByAdmin(email);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/toggle-active/{email:.+}")
    public String toggleUserActive(@PathVariable("email") String email) {
        memberService.toggleUserActiveState(email);
        return "redirect:/admin/users";
    }

    // 여행 계획 관리
    @GetMapping("/trips")
    public String tripManagement(Model model) {
        List<Trip> trips = tripPlanService.findAllTrips();
        model.addAttribute("trips", trips);
        return "admin/trips";
    }

    @PostMapping("/trips/delete/{id}")
    public String deleteTrip(@PathVariable("id") Long tripId) {
        tripPlanService.deleteTripPlanByAdmin(tripId);
        return "redirect:/admin/trips";
    }
}
