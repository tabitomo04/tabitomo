package com.koreatravel.tabitomo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.koreatravel.tabitomo.PathConstants;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import com.koreatravel.tabitomo.service.TravelService;
import com.koreatravel.tabitomo.domain.dto.PlaceDTO;

@RestController
@RequestMapping("/travel")
public class TravelController {

    @Value("${google.api.key}")
    private String googleApiKey;

    @Autowired
    private TravelService travelService;

    @GetMapping(PathConstants.TRAVEL_LIST)
    public String travelList(Model model, @AuthenticationPrincipal String email) {
        // 여행 목록
        return "travel/list";
    }
    
    @GetMapping(PathConstants.TRAVEL_DETAIL)
    public String travelDetail(Model model, @AuthenticationPrincipal String email) {
        // 여행 상세
        return "travel/detail";
    }
    
    @GetMapping(PathConstants.TRAVEL_SIGHT_LIST)
    public String travelSightList(Model model, @AuthenticationPrincipal String email) {
        // 여행 관광지 목록

        Pageable pageable = PageRequest.of(0, 10);
        Page<PlaceDTO> placeList = travelService.getPlaceList(pageable, email);
        model.addAttribute("placeList", placeList);
        return "travel/sight/list";
    }
    
    @GetMapping(PathConstants.TRAVEL_SIGHT_DETAIL)
    public String travelSightDetail(Model model, @AuthenticationPrincipal String email) {
        // 여행 관광지 상세
        return "travel/sight/detail";
    }
    
    @GetMapping(PathConstants.TRAVEL_CITY_LIST)
    public String cityList(Model model) {
        // 도시 목록
        return "city/list";
    }
    
    @GetMapping(PathConstants.TRAVEL_CITY_DETAIL)
    public String cityDetail(Model model, @PathVariable Long id) {
        // 도시 상세
        return "city/detail";
    }
    

    @GetMapping(PathConstants.FAVORITE_PLACE_LIST)
    public String favoritePlaceList(Model model, @AuthenticationPrincipal String email) {
        // 즐겨찾기 목록
        Pageable pageable = PageRequest.of(0, 10);
        Page<PlaceDTO> placeList = travelService.getPlaceList(pageable, email);
        model.addAttribute("placeList", placeList);
        return "favorite/list";
    }
}
