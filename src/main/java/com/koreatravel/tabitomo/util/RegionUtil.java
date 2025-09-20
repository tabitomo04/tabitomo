package com.koreatravel.tabitomo.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

public class RegionUtil {
    public static Set<String> extractRegionsFromTouristSpots(List<Map<String, String>> spots) {
        Set<String> regions = new LinkedHashSet<>();
        for (Map<String, String> spot : spots) {
            String region = spot.getOrDefault("시도명", spot.getOrDefault("CTPRVN_NM", "")).trim();
            if (!region.isEmpty()) {
                regions.add(region);
            }
        }
        return regions;
    }
    public static Set<String> extractRegionsFromAccommodations(List<Map<String, Object>> hotels) {
        Set<String> regions = new LinkedHashSet<>();
        for (Map<String, Object> hotel : hotels) {
            String region = hotel.getOrDefault("CTPRVN_NM", hotel.getOrDefault("시도명", "")).toString().trim();
            if (!region.isEmpty()) {
                regions.add(region);
            }
        }
        return regions;
    }
}
