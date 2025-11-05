package triB.triB.chat.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import triB.triB.auth.entity.User;
import triB.triB.chat.dto.MessageDto;
import triB.triB.chat.dto.MessageResponse;
import triB.triB.chat.dto.PlaceDetail;
import triB.triB.chat.dto.RoomChatResponse;
import triB.triB.chat.entity.MessagePlaceDetail;
import triB.triB.chat.entity.MessageType;
import triB.triB.chat.entity.PlaceTag;
import triB.triB.chat.repository.MessageBookmarkRepository;
import triB.triB.chat.repository.MessagePlaceDetailRepository;
import triB.triB.chat.repository.MessagePlaceRepository;
import triB.triB.chat.repository.MessageRepository;
import triB.triB.friendship.dto.UserResponse;
import triB.triB.room.entity.Room;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;

import java.util.ArrayList;
import java.util.List;
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

    public RoomChatResponse getRoomMessages(Long userId, Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방이 존재하지 않습니다."));

        if (!userRoomRepository.existsByUser_UserIdAndRoom_RoomId(userId, roomId))
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");

        log.info("채팅 내용 조회 시작");
        List<MessageResponse> messages = messageRepository.findAllByRoom_RoomIdOrderByCreatedAtAsc(roomId)
                .stream()
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

    public List<Long> makeTrips(Long roomId){
        return null;
    }

}
