package com.koreatravel.tabitomo.domain.JSON;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator; // JsonCreator import 추가

@Data
@NoArgsConstructor
public class ItineraryItem {
    private int day;
    private String time;
    private String category;
    @JsonProperty("place_name")
    private String placeName;
    private String description;
    private String imageUrl;
    private double latitude;
    private double longitude;
    private String restDate;
    private String useTime;
    private String priceRange;

    // AllArgsConstructor 대신 JsonCreator를 사용하여 명시적으로 JSON 매핑을 처리
    @JsonCreator
    public ItineraryItem(
            @JsonProperty("day") int day,
            @JsonProperty("time") String time,
            @JsonProperty("category") String category,
            @JsonProperty("place_name") String placeName,
            @JsonProperty("description") String description,
            @JsonProperty("image_url") String imageUrl,
            @JsonProperty("latitude") double latitude,
            @JsonProperty("longitude") double longitude,
            @JsonProperty("rest_date") String restDate, // JSON 필드 이름에 맞게 수정
            @JsonProperty("use_time") String useTime,   // JSON 필드 이름에 맞게 수정
            @JsonProperty("price_range") String priceRange // JSON 필드 이름에 맞게 수정
    ) {
        this.day = day;
        this.time = time;
        this.category = category;
        this.placeName = placeName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.restDate = restDate;
        this.useTime = useTime;
        this.priceRange = priceRange;
    }
}
