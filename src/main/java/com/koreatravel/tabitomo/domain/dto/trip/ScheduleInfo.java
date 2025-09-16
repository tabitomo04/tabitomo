package vio.tabitomo.domain.dto.trip;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vio.tabitomo.domain.entity.Place;

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
