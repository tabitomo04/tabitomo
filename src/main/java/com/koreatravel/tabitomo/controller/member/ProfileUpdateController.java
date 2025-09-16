package com.koreatravel.tabitomo.controller.member;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;

@RestController
public class ProfileUpdateController {

    @PostMapping("/member/profile/update")
    public boolean updateProfile(@RequestBody MemberProfileDTO profile) {
        if(true) {
            return true;
        }
        return false;
    }    
}
