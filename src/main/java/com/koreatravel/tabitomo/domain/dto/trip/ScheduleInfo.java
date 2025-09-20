package com.koreatravel.tabitomo.domain.dto.trip;

import com.koreatravel.tabitomo.domain.entity.trip.Place;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int day;
    private String startTime;
    private String endTime;
    private String memo;
    private Place place;
}
