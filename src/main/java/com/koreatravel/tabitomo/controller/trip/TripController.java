package com.koreatravel.tabitomo.controller.trip;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TripController {
    @GetMapping("/tripselect/step1")
    public String tripselectStep1() {
        return "tripselect/step1";
    }
}
