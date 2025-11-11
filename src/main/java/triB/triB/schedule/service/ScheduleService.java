package triB.triB.schedule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triB.triB.room.entity.Room;
import triB.triB.room.entity.UserRoomId;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;
import triB.triB.schedule.dto.ReorderScheduleRequest;
import triB.triB.schedule.dto.ScheduleItemResponse;
import triB.triB.schedule.dto.TripScheduleResponse;
import triB.triB.schedule.dto.VisitStatusUpdateRequest;
import triB.triB.schedule.dto.VisitStatusUpdateResponse;
import triB.triB.schedule.entity.Schedule;
import triB.triB.schedule.entity.Trip;
import triB.triB.schedule.repository.ScheduleRepository;
import triB.triB.schedule.repository.TripRepository;

import java.util.Comparator;
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
     * 일정 방문 순서 변경
     */
    @Transactional
    public TripScheduleResponse reorderSchedule(Long tripId, Long scheduleId, ReorderScheduleRequest request, Long userId) {
        // 권한 검증
        validateUserInTrip(tripId, userId);

        // Schedule 조회 및 dayNumber 확인
        Schedule targetSchedule = scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        Integer dayNumber = targetSchedule.getDayNumber();
        Integer currentOrder = targetSchedule.getVisitOrder();
        Integer newOrder = request.getNewVisitOrder();

        // 같은 순서로 변경하려는 경우 아무 작업 없이 반환
        if (currentOrder.equals(newOrder)) {
            return getTripSchedules(tripId, dayNumber, userId);
        }

        // 해당 날짜의 모든 일정 조회 (visitOrder 순으로 정렬)
        List<Schedule> daySchedules = scheduleRepository.findByTripIdAndDayNumber(tripId, dayNumber)
                .stream()
                .sorted(Comparator.comparing(Schedule::getVisitOrder))
                .collect(Collectors.toList());

        // 새로운 순서가 유효한지 검증
        if (newOrder < 1 || newOrder > daySchedules.size()) {
            throw new IllegalArgumentException("유효하지 않은 방문 순서입니다. (1-" + daySchedules.size() + " 사이여야 합니다)");
        }

        // 순서 변경 로직
        // 1. 리스트에서 대상 일정 제거
        daySchedules.removeIf(s -> s.getScheduleId().equals(scheduleId));

        // 2. 새로운 위치에 삽입 (newOrder는 1-based이므로 index는 newOrder-1)
        daySchedules.add(newOrder - 1, targetSchedule);

        // 3. 모든 일정의 visitOrder 재정렬
        for (int i = 0; i < daySchedules.size(); i++) {
            daySchedules.get(i).setVisitOrder(i + 1);
        }

        // 4. 이동시간 재계산 (다음 Commit에서 구현 예정)
        // recalculateDayTravelTimes(tripId, dayNumber);

        // 5. 업데이트된 일정 반환
        return getTripSchedules(tripId, dayNumber, userId);
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
