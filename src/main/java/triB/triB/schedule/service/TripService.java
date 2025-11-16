package triB.triB.schedule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triB.triB.auth.entity.User;
import triB.triB.budget.entity.TripUserBudget;
import triB.triB.budget.repository.TripUserBudgetRepository;
import triB.triB.room.entity.Room;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;
import triB.triB.schedule.dto.TripListResponse;
import triB.triB.schedule.dto.TripParticipantResponse;
import triB.triB.schedule.entity.Trip;
import triB.triB.schedule.entity.TripFilterType;
import triB.triB.schedule.entity.TripStatus;
import triB.triB.schedule.repository.TripRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final TripRepository tripRepository;
    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final TripUserBudgetRepository tripUserBudgetRepository;

    /**
     * 로그인한 사용자의 승인된 여행 목록 조회
     * @param userId 사용자 ID
     * @return 여행 목록
     */
    public List<TripListResponse> getMyTripList(Long userId) {
        // 1. 사용자의 ACCEPTED 여행 조회
        List<Trip> trips = tripRepository.findByUserIdAndTripStatus(userId, TripStatus.ACCEPTED);

        // 2. 각 Trip을 TripListResponse로 변환
        return trips.stream()
                .map(trip -> buildTripListResponse(trip, userId))
                .collect(Collectors.toList());
    }

    /**
     * 필터링된 여행 목록 조회 (과거/미래)
     * @param userId 사용자 ID
     * @param filterType 필터 타입 (FUTURE, PAST)
     * @return 필터링된 여행 목록
     */
    public List<TripListResponse> getFilteredTripList(Long userId, TripFilterType filterType) {
        List<Trip> trips;

        switch (filterType) {
            case FUTURE:
                // 미래 여행 조회 (READY 상태, startDate 오름차순)
                trips = tripRepository.findFutureTripsByUserId(userId, TripStatus.READY);
                break;
            case PAST:
                // 과거 여행 조회 (ACCEPTED 상태, startDate 내림차순)
                trips = tripRepository.findPastTripsByUserId(userId, TripStatus.ACCEPTED);
                break;
            default:
                // 기본값: 미래 여행
                trips = tripRepository.findFutureTripsByUserId(userId, TripStatus.READY);
        }

        // 각 Trip을 TripListResponse로 변환
        return trips.stream()
                .map(trip -> buildTripListResponse(trip, userId))
                .collect(Collectors.toList());
    }

    /**
     * Trip 엔티티를 TripListResponse로 변환
     */
    private TripListResponse buildTripListResponse(Trip trip, Long userId) {
        // Room 정보 조회
        Room room = roomRepository.findById(trip.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        // 참여자 목록 조회
        List<User> participants = userRoomRepository.findUsersByRoomId(room.getRoomId());
        List<TripParticipantResponse> participantResponses = participants.stream()
                .map(user -> TripParticipantResponse.builder()
                        .userId(user.getUserId())
                        .nickname(user.getNickname())
                        .photoUrl(user.getPhotoUrl())
                        .build())
                .collect(Collectors.toList());

        // 사용자 설정 예산 조회
        BigDecimal userBudget = tripUserBudgetRepository
                .findByTripIdAndUserId(trip.getTripId(), userId)
                .map(TripUserBudget::getAmount)
                .orElse(null);

        // TripListResponse 생성
        return TripListResponse.builder()
                .tripId(trip.getTripId())
                .destination(room.getDestination())
                .startDate(room.getStartDate())
                .endDate(room.getEndDate())
                .participants(participantResponses)
                .budget(trip.getBudget())  // AI 추정 예산
                .userBudget(userBudget)    // 사용자 설정 예산
                .build();
    }
}
