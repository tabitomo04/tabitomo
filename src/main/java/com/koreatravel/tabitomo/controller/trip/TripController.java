package com.koreatravel.tabitomo.controller.trip;

import com.koreatravel.tabitomo.domain.entity.trip.Schedule;
import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import com.koreatravel.tabitomo.service.trip.TripPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class TripController {

    private final TripPlanService tripPlanService;

    @GetMapping("/tripselect/step1")
    public String tripselectStep1() {
        return "tripselect/step1";
    }

    @GetMapping("/trips/{id}")
    public String tripDetail(@PathVariable("id") Long id, Model model) {
        Optional<Trip> tripOpt = tripPlanService.findTripById(id);
        if (tripOpt.isPresent()) {
            Trip trip = tripOpt.get();
            List<Schedule> schedules = tripPlanService.findSchedulesByTripId(id);
            model.addAttribute("trip", trip);
            model.addAttribute("schedules", schedules);
            return "trip-detail";
        } else {
            return "redirect:/member/mypage";
        }
    }
}
