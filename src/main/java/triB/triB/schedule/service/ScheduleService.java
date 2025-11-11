package triB.triB.schedule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triB.triB.room.entity.Room;
import triB.triB.room.entity.UserRoomId;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;
import triB.triB.schedule.dto.ScheduleItemResponse;
import triB.triB.schedule.dto.TripScheduleResponse;
import triB.triB.schedule.dto.VisitStatusUpdateRequest;
import triB.triB.schedule.dto.VisitStatusUpdateResponse;
import triB.triB.schedule.entity.Schedule;
import triB.triB.schedule.entity.Trip;
import triB.triB.schedule.repository.ScheduleRepository;
import triB.triB.schedule.repository.TripRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final TripRepository tripRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRoomRepository userRoomRepository;
    private final RoomRepository roomRepository;

    /**
     * 사용자가 해당 여행에 접근 권한이 있는지 검증
     */
    private void validateUserInTrip(Long tripId, Long userId) {
        // Trip 존재 확인
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));

        // UserRoom 존재 확인 (권한 검증)
        Long roomId = trip.getRoomId();
        UserRoomId userRoomId = new UserRoomId(userId, roomId);

        if (!userRoomRepository.existsById(userRoomId)) {
            throw new IllegalArgumentException("해당 여행에 접근 권한이 없습니다.");
        }
    }

    /**
     * 특정 여행의 특정 날짜 일정 조회
     */
    public TripScheduleResponse getTripSchedules(Long tripId, Integer dayNumber, Long userId) {
        // 권한 검증
        validateUserInTrip(tripId, userId);

        // dayNumber가 null이면 기본값 1 사용
        Integer targetDayNumber = (dayNumber != null) ? dayNumber : 1;

        // Trip 조회
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));

        // Room 조회 (startDate, endDate 획득)
        Room room = roomRepository.findById(trip.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 특정 날짜의 일정 조회
        List<Schedule> schedules = scheduleRepository.findByTripIdAndDayNumber(tripId, targetDayNumber);

        // Schedule 리스트를 ScheduleItemResponse로 매핑
        List<ScheduleItemResponse> scheduleItems = schedules.stream()
                .map(this::mapToScheduleItemResponse)
                .collect(Collectors.toList());

        // TripScheduleResponse 생성 및 반환
        return TripScheduleResponse.builder()
                .tripId(trip.getTripId())
                .destination(trip.getDestination())
                .startDate(room.getStartDate())
                .endDate(room.getEndDate())
                .currentDay(targetDayNumber)
                .schedules(scheduleItems)
                .build();
    }

    /**
     * 일정의 방문 완료 상태 변경
     */
    @Transactional
    public VisitStatusUpdateResponse updateVisitStatus(Long tripId, Long scheduleId, VisitStatusUpdateRequest request, Long userId) {
        // 권한 검증
        validateUserInTrip(tripId, userId);

        // Schedule 조회
        Schedule schedule = scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        // 방문 상태 업데이트
        schedule.setIsVisit(request.getIsVisit());

        // JPA dirty checking으로 자동 업데이트

        // 응답 DTO 생성 및 반환
        return VisitStatusUpdateResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .isVisit(schedule.getIsVisit())
                .build();
    }

    /**
     * Schedule 엔티티를 ScheduleItemResponse로 매핑
     */
    private ScheduleItemResponse mapToScheduleItemResponse(Schedule schedule) {
        return ScheduleItemResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .displayName(schedule.getPlaceName())
                .arrival(schedule.getArrival())
                .departure(schedule.getDeparture())
                .placeTag(schedule.getPlaceTag())
                .travelTime(schedule.getTravelTime())
                .visitOrder(schedule.getVisitOrder())
                .isVisit(schedule.getIsVisit())
                .build();
    }
}
