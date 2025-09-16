package vio.tabitomo.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vio.tabitomo.domain.entity.Place;
import vio.tabitomo.repository.PlaceRepository;

import java.util.Arrays;
import java.util.List;

@Controller
public class TripInformationController {

    private final PlaceRepository placeRepository;

    public TripInformationController(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    @GetMapping("/tripinformation")
    public String showTripInformation(Model model,
                                      @RequestParam(value = "region", required = false, defaultValue = "all") String region,
                                      @RequestParam(value = "categoryCode", required = false, defaultValue = "all") String categoryCode,
                                      @RequestParam(value = "keyword", required = false) String keyword,
                                      @PageableDefault(size = 10) Pageable pageable) {

        Page<Place> placesPage = placeRepository.findByFilters(region, categoryCode, keyword, pageable);
        model.addAttribute("placesPage", placesPage);

        // Filters for the view
        List<String> regions = Arrays.asList(
            "대구광역시", "경기도", "경상남도", "경상북도", "전북특별자치도",
            "강원특별자치도", "충청북도", "전라남도", "충청남도", "서울특별시",
            "울산광역시", "광주광역시", "부산광역시", "제주특별자치도",
            "대전광역시", "인천광역시", "세종특별자치시"
        );
        model.addAttribute("regions", regions);

        List<String> categories = Arrays.asList(
            "유명사적/유적지", "캠핑장", "폭포/계곡", "비/탑/문/각", "관광안내소/매표소",
            "항공사/여행사", "일반관광지", "성/성터", "고택/생가/민속마을", "관광농원/허브마을",
            "서원/향교/서당", "정보화마을", "먹거리/패션거리", "휴양림/수목원", "지역축제",
            "식물원", "천연기념물", "팜스테이", "왕릉/고분", "일반유원지/일반놀이공원",
            "드라마/영화촬영지", "야영장", "유명관광지", "영어마을", "테마공원/대형놀이공원",
            "해수욕장", "동물원", "글램핑코리아(캠핑)", "온천지역", "아쿠아리움/대형수족관",
            "보물", "캠핑홀리데이(캠핑)", "궁궐/종묘", "N", "잼핑홀리데이(캠핑)", "국보"
        );
        model.addAttribute("categories", categories);

        // To keep filter state in the view
        model.addAttribute("selectedRegion", region);
        model.addAttribute("selectedCategoryCode", categoryCode);
        model.addAttribute("keyword", keyword);

        return "tripinformation/tripinformation";
    }
}
