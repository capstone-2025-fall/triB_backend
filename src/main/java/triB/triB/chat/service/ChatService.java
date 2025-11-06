package triB.triB.chat.service;

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
import triB.triB.auth.entity.User;
import triB.triB.chat.dto.*;
import triB.triB.chat.entity.*;
import triB.triB.chat.repository.MessageBookmarkRepository;
import triB.triB.chat.repository.MessagePlaceDetailRepository;
import triB.triB.chat.repository.MessagePlaceRepository;
import triB.triB.chat.repository.MessageRepository;
import triB.triB.friendship.dto.UserResponse;
import triB.triB.room.entity.Room;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;
import triB.triB.schedule.entity.Schedule;
import triB.triB.schedule.entity.Trip;
import triB.triB.schedule.entity.TripStatus;
import triB.triB.schedule.repository.ScheduleRepository;
import triB.triB.schedule.repository.TripRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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

    public RoomChatResponse getRoomMessages(Long userId, Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방이 존재하지 않습니다."));

        if (!userRoomRepository.existsByUser_UserIdAndRoom_RoomId(userId, roomId))
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");

        log.info("채팅 내용 조회 시작");
        List<MessageResponse> messages = messageRepository.findAllByRoom_RoomIdOrderByCreatedAtAsc(roomId)
                .stream()
                .filter(Objects::nonNull)
                .map(message -> {
                    User user = message.getUser();

                    PlaceTag tag = messagePlaceRepository.findPlaceTagByMessage_MessageId(message.getMessageId());
                    Boolean isBookmarked = messageBookmarkRepository.findByMessage_MessageId(message.getMessageId()) != null;
                    PlaceDetail placeDetail = message.getMessageType().equals(MessageType.TEXT) ? null : makePlaceDetail(message.getMessageId());

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
                .messages(messages)
                .build();
    }

    // todo 일단은 조회 동기적으로 구현해서 올리고 추후 비동기로 수정하자(디비 조회가 동기적이여서 노란줄)
    // todo 일단 RestAPI로 생성하고 일정 생성 했음을 알리는 걸 WebSocket으로 뿌리자
    @Transactional
    public Mono<Long> makeTrip(Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new EntityNotFoundException("해당 룸이 존재하지 않습니다."));

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

            // 장소 태그가 저장 되어있고 북마크 되어있음
            if (place != null && bookmark != null)
                mustVisit.add(message.getContent());
            // 장소태그만 저장되어있음
            else if (place != null)
                places.add(new ModelRequest.ModelPlaceRequest(message.getContent(), place.getPlaceTag()));
            // 북마크만 되어있음
            else if (bookmark != null)
                rule.add(message.getContent());

            chat.add(message.getContent());
        }

        return aiModelWebClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ModelRequest.builder()
                        .days((int) ChronoUnit.DAYS.between(room.getStartDate(), room.getEndDate()) + 1)
                        .startDate(room.getStartDate())
                        .country(room.getDestination())
                        .members(userRoomRepository.countByRoom_RoomId(roomId))
                        .places(places)
                        .mustVisit(mustVisit)
                        .rule(rule)
                        .chat(chat)
                        .build()
                )
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        res -> res.createException().flatMap(Mono::error)
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        res -> res.createException().flatMap(Mono::error)
                )
                .bodyToMono(ModelResponse.class)
                .map(body -> {
                    //todo trip 저장
                    Trip t = Trip.builder()
                            .roomId(roomId)
                            .destination(room.getDestination())
                            .tripStatus(TripStatus.READY)
                            .build();

                    tripRepository.save(t);

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
                                                    .nameAddress(visit.getNameAddress())
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
                });
    }

    private PlaceDetail makePlaceDetail(Long messageId) {
        MessagePlaceDetail mpd = messagePlaceDetailRepository.findByMessage_MessageId(messageId);

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
