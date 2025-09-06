package com.koreatravel.tabitomo.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 대한민국 주요 도시별 위도/경도 경계값 (사각형 박스 기준)
 * 실제 서비스에서는 더 정밀한 값 또는 GeoJSON 폴리곤을 사용할 수 있습니다.
 */
public class CityFilterUtil {
    // 도시별 경계값 맵
    public static final Map<String, CityBounds> CITY_BOUNDS_MAP = Map.ofEntries(
        Map.entry("서울", new CityBounds(37.4, 37.7, 126.7, 127.2)),
        Map.entry("부산", new CityBounds(35.0, 35.3, 128.8, 129.3)),
        Map.entry("대구", new CityBounds(35.7, 36.0, 128.4, 128.8)),
        Map.entry("광주", new CityBounds(35.0, 35.3, 126.7, 127.1)),
        Map.entry("인천", new CityBounds(37.3, 37.6, 126.5, 126.8)),
        Map.entry("대전", new CityBounds(36.2, 36.5, 127.2, 127.5)),
        Map.entry("울산", new CityBounds(35.3, 35.7, 129.0, 129.5)),
        Map.entry("세종", new CityBounds(36.4, 36.7, 127.2, 127.4)),
        Map.entry("수원", new CityBounds(37.2, 37.4, 126.9, 127.1)),
        Map.entry("춘천", new CityBounds(37.7, 37.9, 127.6, 127.8)),
        Map.entry("강릉", new CityBounds(37.6, 37.9, 128.8, 129.2)),
        Map.entry("전주", new CityBounds(35.7, 35.9, 127.0, 127.2)),
        Map.entry("포항", new CityBounds(36.0, 36.2, 129.2, 129.5)),
        Map.entry("창원", new CityBounds(35.1, 35.4, 128.5, 128.8)),
        Map.entry("제주", new CityBounds(33.2, 33.6, 126.2, 126.7))
        // 필요시 추가
    );

    /**
     * 일정/숙소 리스트를 도시명 기준으로 필터링
     */
    public static <T extends HasLatLng> List<T> filterByCity(String city, List<T> list) {
        CityBounds bounds = CITY_BOUNDS_MAP.get(city);
        if (bounds == null) return list; // 범위 정보 없으면 필터링 안함
        return list.stream()
            .filter(item ->
                item.getLatitude() != null && item.getLongitude() != null &&
                item.getLatitude() >= bounds.minLat && item.getLatitude() <= bounds.maxLat &&
                item.getLongitude() >= bounds.minLng && item.getLongitude() <= bounds.maxLng
            )
            .collect(Collectors.toList());
    }

    /**
     * 위도/경도 getter 인터페이스
     */
    public interface HasLatLng {
        Double getLatitude();
        Double getLongitude();
    }
}

class CityBounds {
    public final double minLat, maxLat, minLng, maxLng;
    public CityBounds(double minLat, double maxLat, double minLng, double maxLng) {
        this.minLat = minLat; this.maxLat = maxLat; this.minLng = minLng; this.maxLng = maxLng;
    }
}
