package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.member.MemberUpdateDTO;
import com.koreatravel.tabitomo.service.member.MemberService;
import com.koreatravel.tabitomo.service.reference.ReferenceDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final ReferenceDataService referenceDataService;

    /**
     * 회원 프로필 조회
     */
    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        MemberProfileDTO profile = memberService.getMemberProfile(email);
        model.addAttribute("profile", profile);
        return "member/profile";
    }

    /**
     * 회원 정보 수정 폼
     */
    @GetMapping("/edit")
    public String editProfileForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        MemberProfileDTO profile = memberService.getMemberProfile(email);
        
        // Add reference data for the form
        model.addAttribute("updateForm", profile);
        model.addAttribute("countries", referenceDataService.getAllCountries());
        model.addAttribute("languages", referenceDataService.getAllLanguages());
        
        return "member/edit";
    }

    /**
     * 회원 정보 수정 처리
     */
    @PostMapping("/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Validated @ModelAttribute("updateForm") MemberUpdateDTO updateDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in update form: {}", bindingResult.getAllErrors());
            return "member/edit";
        }

        try {
            String email = userDetails.getUsername();
            memberService.updateMemberProfile(email, updateDTO);
            redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 수정되었습니다.");
            return "redirect:/member/profile";
        } catch (Exception e) {
            log.error("Error updating member profile: {}", e.getMessage(), e);
            bindingResult.reject("update.failed", "회원 정보 수정 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "member/edit";
        }
    }

    /**
     * 회원 탈퇴
     */
    @PostMapping("/withdraw")
    public String withdrawMember(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            String email = userDetails.getUsername();
            memberService.deactivateMember(email);
            redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다.");
            return "redirect:/auth/logout";
        } catch (Exception e) {
            log.error("Error withdrawing member: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "회원 탈퇴 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:/member/profile";
        }
    }
}
