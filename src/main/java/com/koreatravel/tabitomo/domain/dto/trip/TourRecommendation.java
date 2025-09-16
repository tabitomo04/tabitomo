package vio.tabitomo.domain.dto.trip;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vio.tabitomo.domain.entity.Place;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourRecommendation {
    private String title;
    private List<ScheduleInfo> itinerary;
    private Place accommodation;
}
