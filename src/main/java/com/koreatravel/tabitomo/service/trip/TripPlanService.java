package com.koreatravel.tabitomo.service.trip;

import com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.trip.PlaceEntity;
import com.koreatravel.tabitomo.domain.entity.trip.ScheduleEntity;
import com.koreatravel.tabitomo.domain.entity.trip.TripEntity;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.trip.PlaceRepository;
import com.koreatravel.tabitomo.repository.trip.ScheduleRepository;
import com.koreatravel.tabitomo.repository.trip.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripPlanService {

    private final MemberRepository memberRepository;
    private final TripRepository tripRepository;
    private final PlaceRepository placeRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public Long saveTripPlan(com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO tripPlanDTO, String email) {
        log.info("Saving trip plan for user: {}", email);
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + email));

        // 1. Find or create the accommodation Place entity using the rich data from the DTO
        PlaceEntity accommodationPlace = null;
        if (tripPlanDTO.getAccommodation() != null) {
            accommodationPlace = findOrCreatePlaceFromAccommodationDto(tripPlanDTO.getAccommodation());
        }

        // 2. Build and save the Trip entity with the accommodation
        String tripTitle = (tripPlanDTO.getDestination() != null && !tripPlanDTO.getDestination().isEmpty())
                ? tripPlanDTO.getDestination() + " 여행"
                : "새로운 여행 계획";

        TripEntity trip = TripEntity.builder()
                .member(member)
                .title(tripTitle)
                .startDate(tripPlanDTO.getStartDate())
                .endDate(tripPlanDTO.getEndDate())
                .accommodation(accommodationPlace)
                .visibility("PRIVATE")
                .build();
        tripRepository.save(trip);

        // 3. Save schedules
        if (tripPlanDTO.getDailySchedules() != null) {
            for (TripPlanDTO.DailySchedule dailySchedule : tripPlanDTO.getDailySchedules()) {
                for (TripPlanDTO.ScheduleItem item : dailySchedule.getSchedules()) {
                    PlaceEntity place = findOrCreatePlaceFromScheduleItem(item);

                    ScheduleEntity schedule = ScheduleEntity.builder()
                            .trip(trip)
                            .place(place)
                            .day(dailySchedule.getDay())
                            .startTime(item.getStartTime())
                            .endTime(item.getEndTime() != null ? item.getEndTime() : "") // Use end time if available
                            .memo(item.getDescription())
                            .build();
                    scheduleRepository.save(schedule);
                }
            }
        }

        return trip.getId();
    }

    private PlaceEntity findOrCreatePlaceFromAccommodationDto(
            com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO.Accommodation accDto) {
        return placeRepository.findByName(accDto.getPlaceName())
                .orElseGet(() -> {
                    PlaceEntity newPlace = PlaceEntity.builder()
                            .name(accDto.getPlaceName())
                            .description(accDto.getDescription())
                            .imageUrl(accDto.getImageUrl())
                            .address(accDto.getAddress() != null ? accDto.getAddress() : "")
                            .latitude(accDto.getLatitude() != null ? accDto.getLatitude() : 0.0)
                            .longitude(accDto.getLongitude() != null ? accDto.getLongitude() : 0.0)
                            .categoryCode("ACCOMMODATION")
                            .build();
                    return placeRepository.save(newPlace);
                });
    }

    @Transactional
    public void updateTripPlan(Long tripId, TripPlanDTO tripPlanDTO, String email) {
        log.info("Updating trip plan with ID: {} for user: {}", tripId, email);
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 여행을 찾을 수 없습니다."));

        // Check if the trip belongs to the user
        if (!trip.getMember().getEmail().equals(email)) {
            throw new AccessDeniedException("이 여행 계획을 수정할 권한이 없습니다.");
        }

        // Update trip details
        if (tripPlanDTO.getPlanName() != null && !tripPlanDTO.getPlanName().isEmpty()) {
            trip.setTitle(tripPlanDTO.getPlanName());
        } else if (tripPlanDTO.getDestination() != null && !tripPlanDTO.getDestination().isEmpty()) {
            trip.setTitle(tripPlanDTO.getDestination() + " 여행");
        }

        // Update accommodation if provided
        if (tripPlanDTO.getAccommodation() != null) {
            PlaceEntity accommodationPlace = findOrCreatePlaceFromAccommodationDto(tripPlanDTO.getAccommodation());
            trip.setAccommodation(accommodationPlace);
        }

        // Update dates if provided
        if (tripPlanDTO.getStartDate() != null) {
            trip.setStartDate(tripPlanDTO.getStartDate());
        }
        if (tripPlanDTO.getEndDate() != null) {
            trip.setEndDate(tripPlanDTO.getEndDate());
        }

        // Clear existing schedules
        scheduleRepository.deleteByTrip(trip);
        trip.getSchedules().clear();

        // Add new schedules
        if (tripPlanDTO.getDailySchedules() != null) {
            for (TripPlanDTO.DailySchedule dailySchedule : tripPlanDTO.getDailySchedules()) {
                for (TripPlanDTO.ScheduleItem item : dailySchedule.getSchedules()) {
                    PlaceEntity place = findOrCreatePlaceFromScheduleItem(item);

                    ScheduleEntity schedule = ScheduleEntity.builder()
                            .trip(trip)
                            .place(place)
                            .day(dailySchedule.getDay())
                            .startTime(item.getStartTime())
                            .endTime(item.getEndTime() != null ? item.getEndTime() : "")
                            .memo(item.getDescription())
                            .build();
                    scheduleRepository.save(schedule);
                }
            }
        }

        log.info("Successfully updated trip plan with ID: {}", trip.getId());
    }

    @Transactional(readOnly = true)
    public List<TripEntity> findTripsByMemberEmail(String email) {
        log.debug("Finding all trips for user: {}", email);
        return tripRepository.findByMemberEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<TripEntity> findTripByIdAndMemberEmail(Long tripId, String email) {
        log.debug("Finding trip with ID: {} for user: {}", tripId, email);
        return tripRepository.findById(tripId)
                .filter(trip -> trip.getMember().getEmail().equals(email));
    }

    @Transactional(readOnly = true)
    public List<ScheduleEntity> findSchedulesByTripId(Long tripId) {
        log.debug("Finding schedules for trip ID: {}", tripId);
        return scheduleRepository.findByTripId(tripId);
    }

    @Transactional
    public void deleteTripPlan(Long tripId, String email) {
        log.info("Deleting trip with ID: {} for user: {}", tripId, email);
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 여행을 찾을 수 없습니다."));
        
        // Check if the trip belongs to the user
        if (!trip.getMember().getEmail().equals(email)) {
            throw new AccessDeniedException("이 여행 계획을 삭제할 권한이 없습니다.");
        }
        
        // Delete all schedules first to avoid constraint violations
        scheduleRepository.deleteByTrip(trip);
        
        // Then delete the trip
        tripRepository.delete(trip);
        log.info("Successfully deleted trip with ID: {}", tripId);
    }

    private PlaceEntity findOrCreatePlaceFromScheduleItem(TripPlanDTO.ScheduleItem itemDto) {
        return placeRepository.findByName(itemDto.getPlace())
                .orElseGet(() -> {
                    PlaceEntity newPlace = PlaceEntity.builder()
                            .name(itemDto.getPlace())
                            .description(itemDto.getDescription() != null ? itemDto.getDescription() : "")
                            .address(itemDto.getAddress() != null ? itemDto.getAddress() : "")
                            .latitude(itemDto.getLatitude())
                            .longitude(itemDto.getLongitude())
                            .categoryCode("ATTRACTION")
                            .build();
                    return placeRepository.save(newPlace);
                });
    }
    
    /**
     * Convert TripEntity to TripPlanDTO
     */
    public TripPlanDTO convertToDTO(TripEntity trip) {
        TripPlanDTO dto = new TripPlanDTO();
        dto.setPlanName(trip.getTitle());
        // Destination is now part of the accommodation
        if (trip.getAccommodation() != null) {
            dto.setDestination(trip.getAccommodation().getName());
        }
        dto.setStartDate(trip.getStartDate());
        dto.setEndDate(trip.getEndDate());
        
        // Set accommodation if exists
        if (trip.getAccommodation() != null) {
            TripPlanDTO.Accommodation accDto = new TripPlanDTO.Accommodation();
            accDto.setPlaceName(trip.getAccommodation().getName());
            accDto.setAddress(trip.getAccommodation().getAddress());
            accDto.setDescription(trip.getAccommodation().getDescription());
            accDto.setImageUrl(trip.getAccommodation().getImageUrl());
            accDto.setLatitude(trip.getAccommodation().getLatitude());
            accDto.setLongitude(trip.getAccommodation().getLongitude());
            dto.setAccommodation(accDto);
        }
        
        // Set daily schedules
        List<ScheduleEntity> schedules = scheduleRepository.findByTripId(trip.getId());
        if (schedules != null && !schedules.isEmpty()) {
            // Group schedules by day
            Map<Integer, List<ScheduleEntity>> schedulesByDay = schedules.stream()
                    .collect(Collectors.groupingBy(ScheduleEntity::getDay));
            
            List<TripPlanDTO.DailySchedule> dailySchedules = new ArrayList<>();
            
            for (Map.Entry<Integer, List<ScheduleEntity>> entry : schedulesByDay.entrySet()) {
                TripPlanDTO.DailySchedule dailySchedule = new TripPlanDTO.DailySchedule();
                dailySchedule.setDay(entry.getKey());
                
                List<TripPlanDTO.ScheduleItem> scheduleItems = new ArrayList<>();
                
                for (ScheduleEntity schedule : entry.getValue()) {
                    TripPlanDTO.ScheduleItem item = new TripPlanDTO.ScheduleItem();
                    item.setStartTime(schedule.getStartTime());
                    item.setEndTime(schedule.getEndTime());
                    item.setPlace(schedule.getPlace().getName());
                    item.setDescription(schedule.getMemo());
                    item.setAddress(schedule.getPlace().getAddress());
                    item.setLatitude(schedule.getPlace().getLatitude());
                    item.setLongitude(schedule.getPlace().getLongitude());
                    
                    scheduleItems.add(item);
                }
                
                dailySchedule.setSchedules(scheduleItems);
                dailySchedules.add(dailySchedule);
            }
            
            // Sort daily schedules by day
            dailySchedules.sort(Comparator.comparingInt(TripPlanDTO.DailySchedule::getDay));
            dto.setDailySchedules(dailySchedules);
        }
        
        return dto;
    }
}
