package com.koreantravel.tabitomo.domain.dto.trip;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.koreantravel.tabitomo.domain.entity.Place;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInfo {
    private int day;
    private String startTime;
    private String endTime;
    private String memo;
    private Place place;
}
