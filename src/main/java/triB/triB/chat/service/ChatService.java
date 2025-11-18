package triB.triB.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Date;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.UserRepository;
import triB.triB.chat.dto.*;
import triB.triB.chat.entity.*;
import triB.triB.chat.repository.MessageBookmarkRepository;
import triB.triB.chat.repository.MessagePlaceDetailRepository;
import triB.triB.chat.repository.MessagePlaceRepository;
import triB.triB.chat.repository.MessageRepository;
import triB.triB.community.entity.Post;
import triB.triB.community.repository.PostRepository;
import triB.triB.friendship.dto.UserResponse;
import triB.triB.global.exception.CustomException;
import triB.triB.global.exception.ErrorCode;
import triB.triB.global.infra.RedisClient;
import triB.triB.room.entity.Room;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;
import triB.triB.schedule.entity.Schedule;
import triB.triB.schedule.entity.Trip;
import triB.triB.schedule.entity.TripStatus;
import triB.triB.schedule.entity.VersionStatus;
import triB.triB.schedule.repository.ScheduleRepository;
import triB.triB.schedule.repository.TripRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final MessageRepository messageRepository;
    private final MessageBookmarkRepository messageBookmarkRepository;
    private final MessagePlaceRepository messagePlaceRepository;
    private final MessagePlaceDetailRepository messagePlaceDetailRepository;
    private final @Qualifier("aiModelWebClient") WebClient aiModelWebClient;
    private final ScheduleRepository scheduleRepository;
    private final TripRepository tripRepository;
    private final RedisClient redisClient;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public RoomChatResponse getRoomMessages(Long userId, Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방이 존재하지 않습니다."));

        if (!userRoomRepository.existsByUser_UserIdAndRoom_RoomId(userId, roomId))
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");

        log.info("채팅 내용 조회 시작");

        List<Message> messages = messageRepository.findAllByRoom_RoomIdOrderByCreatedAtAsc(roomId);
        List<Long> messageIds = messages.stream()
                .map(Message::getMessageId)
                .toList();

        Map<Long, PlaceTag> placeTagMap = messagePlaceRepository.findByMessageIds(messageIds).stream()
                .collect(Collectors.toMap(mp -> mp.getMessage().getMessageId(), MessagePlace::getPlaceTag));

        Map<Long, Boolean> bookmarkMap = messageBookmarkRepository.findByMessageIds(messageIds).stream()
                .collect(Collectors.toMap(mb -> mb.getMessage().getMessageId(), mb -> true));

        Map<Long, MessagePlaceDetail> placeDetailMap = messagePlaceDetailRepository.findByMessageIds(messageIds).stream()
                .collect(Collectors.toMap(mpd -> mpd.getMessage().getMessageId(), mpd -> mpd));

        List<MessageResponse> response = messages.stream()
                .filter(Objects::nonNull)
                .map(message -> {
                    User user = message.getUser();
                    PlaceTag tag = placeTagMap.getOrDefault(message.getMessageId(), null);
                    Boolean isBookmarked = bookmarkMap.getOrDefault(message.getMessageId(), false);
                    PlaceDetail placeDetail = makePlaceDetail(placeDetailMap.getOrDefault(message.getMessageId(), null));

                    return MessageResponse.builder()
                            .actionType(null)
                            .user(new UserResponse(user.getUserId(), user.getNickname(), user.getPhotoUrl()))
                            .message(
                                    MessageDto.builder()
                                            .messageId(message.getMessageId())
                                            .content(message.getContent())
                                            .messageType(message.getMessageType())
                                            .messageStatus(message.getMessageStatus())
                                            .tag(tag)
                                            .isBookmarked(isBookmarked)
                                            .placeDetail(placeDetail)
                                            .build()
                            )
                            .createdAt(message.getCreatedAt())
                            .build();
                })
                .toList();

        return RoomChatResponse.builder()
                .roomName(room.getRoomName())
                .messages(response)
                .build();
    }

    // todo 일단 RestAPI로 생성하고 일정 생성 했음을 알리는 걸 WebSocket으로 뿌리자
    @Transactional
    public Mono<Long> makeTrip(Long userId, Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new EntityNotFoundException("해당 룸이 존재하지 않습니다."));

        if (!userRoomRepository.existsByUser_UserIdAndRoom_RoomId(userId, roomId))
            throw new BadCredentialsException("해당 권한이 없습니다.");

        // Redis에 설정된 락있는지 확인
        Boolean locked = redisClient.setIfAbsent("trip:create:lock", String.valueOf(roomId), String.valueOf(TripCreateStatus.WAITING),600);

        if (!locked){
            throw new CustomException(ErrorCode.TRIP_CREATING_IN_PROGRESS);
        }

        try {
            List<Message> messages = messageRepository.findAllByRoom_RoomIdOrderByCreatedAtAsc(roomId);

            List<ModelRequest.ModelPlaceRequest> places = new ArrayList<>();
            List<String> mustVisit = new ArrayList<>();
            List<String> rule = new ArrayList<>();
            List<String> chat = new ArrayList<>();

            MessagePlace place = null;
            MessageBookmark bookmark = null;

            for (Message message : messages) {
                place = messagePlaceRepository.findByMessage_MessageId(message.getMessageId());
                bookmark = messageBookmarkRepository.findByMessage_MessageId(message.getMessageId());

                String content = message.getContent();
                // 장소 태그가 저장 되어있고 북마크 되어있음
                if (place != null && bookmark != null)
                    mustVisit.add(content);
                    // 장소태그만 저장되어있음
                else if (place != null)
                    places.add(new ModelRequest.ModelPlaceRequest(content, place.getPlaceTag()));
                    // 북마크만 되어있음
                else if (bookmark != null)
                    rule.add(content);

                // 커뮤니티에서 가져온 일정인 경우
                if (message.getMessageType().equals(MessageType.COMMUNITY_SHARE)){
                    Post p = postRepository.findById(Long.parseLong(content))
                            .orElseThrow(() -> new EntityNotFoundException("해당 게시글이 존재하지 않습니다."));
                    Long tripId = p.getTripId();

                    scheduleRepository.findByTripIdOrderByDayNumberAscVisitOrderAsc(tripId).stream()
                            .filter(s -> s.getPlaceTag() != PlaceTag.HOME)
                            .forEach(s -> places.add(new ModelRequest.ModelPlaceRequest(s.getPlaceName(), s.getPlaceTag())));
                } else {
                    chat.add(content);
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            log.info("places: {}", mapper.writeValueAsString(places));

            ModelRequest modelRequest = ModelRequest.builder()
                    .days((int) ChronoUnit.DAYS.between(room.getStartDate(), room.getEndDate()) + 1)
                    .startDate(room.getStartDate().toString())
                    .country(room.getDestination())
                    .members(userRoomRepository.countByRoom_RoomId(roomId))
                    .places(places)
                    .mustVisit(mustVisit)
                    .rule(rule)
                    .chat(chat)
                    .build();

            log.info("모델 통신 시작");
            return aiModelWebClient.post()
                    .uri("/api/v2/itinerary/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(modelRequest)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            res -> res.createException().flatMap(e -> {
                                log.error("AI 모델 요청 오류: {}", e.getMessage());
                                return Mono.error(new CustomException(ErrorCode.MODEL_REQUEST_ERROR));
                            })
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            res -> res.createException().flatMap(e -> {
                                log.error("AI 모델 서버 오류: {}", e.getMessage());
                                return Mono.error(new CustomException(ErrorCode.MODEL_ERROR));
                            })
                    )
                    .bodyToMono(ModelResponse.class)
                    .map(body -> saveTripAndSchedule(room, body))
                    .doFinally(signal -> {
                        redisClient.deleteData("trip:create:lock", String.valueOf(roomId));
                        log.info("락 해제 완료: signal = {}", signal);
                    })
                    .onErrorResume(e -> {
                        if (e instanceof CustomException) {
                            log.error("에러 발생: {}", e.getMessage());
                            return Mono.error(e);
                        }
                        log.error("Trip 저장 실패", e.getMessage());
                        return Mono.error(new CustomException(ErrorCode.TRIP_SAVE_FAIL));
                    });
        } catch (Exception e){
            redisClient.deleteData("trip:create:lock", String.valueOf(roomId));
            log.error("Trip 생성 중 오류 발생", e);
            return Mono.error(new CustomException(ErrorCode.TRIP_PREPARATION_FAILED));
        }
    }

    @Transactional
    protected Long saveTripAndSchedule(Room room, ModelResponse body) {
        Trip existingTrip = tripRepository.findByRoomId(room.getRoomId());
        if (existingTrip != null){
            existingTrip.setVersionStatus(VersionStatus.OLD);
            tripRepository.save(existingTrip);
        }
        Trip t = Trip.builder()
                .roomId(room.getRoomId())
                .destination(room.getDestination())
                .versionStatus(VersionStatus.NEW)
                .travelMode(body.getTravelMode())
                .budget(body.getBudget())
                .build();
        tripRepository.save(t);

        // DB에 성공적으로 저장
        body.getItinerary().stream()
                .forEach(itinerary -> {
                    itinerary.getVisits()
                            .forEach((visit) -> {
                                LocalDate date = room.getStartDate()
                                        .plusDays(itinerary.getDay() - 1); // + dayNumber - 1
                                LocalDateTime arrival = date.atTime(LocalTime.parse(visit.getArrival()));
                                LocalDateTime departure = date.atTime(LocalTime.parse(visit.getDeparture()));

                                Schedule schedule = Schedule.builder()
                                        .tripId(t.getTripId())
                                        .dayNumber(itinerary.getDay())
                                        .date(date)
                                        .visitOrder(visit.getOrder())
                                        .placeName(visit.getDisplayName())
                                        .placeTag(visit.getPlaceTag())
                                        .latitude(visit.getLatitude())
                                        .longitude(visit.getLongitude())
                                        .isVisit(false)
                                        .arrival(arrival)
                                        .departure(departure)
                                        .travelTime(String.valueOf(visit.getTravelTime()))
                                        .build();
                                scheduleRepository.save(schedule);
                            });
                });

        return t.getTripId();
    }

    public TripCreateStatusResponse getTripStatus(Long userId, Long roomId) {
        if (!userRoomRepository.existsByUser_UserIdAndRoom_RoomId(userId, roomId))
            throw new BadCredentialsException("해당 권한이 없습니다.");
        Trip t;
        if (redisClient.getData("trip:create:lock", String.valueOf(roomId)) != null)
            return new TripCreateStatusResponse(TripCreateStatus.WAITING, null);
        else if ((t = tripRepository.findByRoomId(roomId)) != null)
            return new TripCreateStatusResponse(TripCreateStatus.SUCCESS, t.getTripId());
        else
            return new TripCreateStatusResponse(TripCreateStatus.NOT_STARTED, null);
    }

    private PlaceDetail makePlaceDetail(MessagePlaceDetail mpd){
        if (mpd == null)
            return null;

        return PlaceDetail.builder()
                .placeId(mpd.getPlaceId())
                .displayName(mpd.getDisplayName())
                .latitude(mpd.getLatitude())
                .longitude(mpd.getLongitude())
                .photoUrl(mpd.getPhotoUrl())
                .build();
    }
}
