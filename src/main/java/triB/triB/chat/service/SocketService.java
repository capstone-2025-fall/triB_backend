package triB.triB.chat.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triB.triB.auth.entity.IsAlarm;
import triB.triB.auth.entity.Token;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.TokenRepository;
import triB.triB.auth.repository.UserRepository;
import triB.triB.chat.dto.ActionType;
import triB.triB.chat.dto.MessageDto;
import triB.triB.chat.dto.MessageResponse;
import triB.triB.chat.dto.PlaceDetail;
import triB.triB.chat.entity.*;
import triB.triB.chat.repository.MessageBookmarkRepository;
import triB.triB.chat.repository.MessagePlaceDetailRepository;
import triB.triB.chat.repository.MessagePlaceRepository;
import triB.triB.chat.repository.MessageRepository;
import triB.triB.friendship.dto.UserResponse;
import triB.triB.global.fcm.FcmSendRequest;
import triB.triB.global.fcm.FcmSender;
import triB.triB.global.fcm.RequestType;
import triB.triB.room.entity.Room;
import triB.triB.room.entity.UserRoom;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketService {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;
    private final MessageBookmarkRepository messageBookmarkRepository;
    private final MessagePlaceRepository messagePlaceRepository;
    private final MessagePlaceDetailRepository messagePlaceDetailRepository;
    private final TokenRepository tokenRepository;
    private final FcmSender fcmSender;

    // 메세지 전송
    @Transactional
    public MessageResponse sendMessageToRoom(Long userId, Long roomId, String content) throws FirebaseMessagingException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방이 존재하지 않습니다."));

        Message message = Message.builder()
                .room(room)
                .user(user)
                .messageType(MessageType.TEXT)
                .messageStatus(MessageStatus.ACTIVE)
                .content(content)
                .build();
        messageRepository.save(message);

        sendMessagePushNotification(roomId, message);
        // todo 확인해야할점: 지금 이 채팅방을 구독하고 있는 사람들에게는 메세지를 보낼필요가없음. 이 방안에 들어있지만 안 구독하고있는 사람에게 알림을 보내야됨

        return MessageResponse.builder()
                .actionType(ActionType.NEW_MESSAGE)
                .user(new UserResponse(userId, user.getNickname(), user.getPhotoUrl()))
                .message(
                        MessageDto.builder()
                                .messageId(message.getMessageId())
                                .content(message.getContent())
                                .messageType(message.getMessageType())
                                .messageStatus(MessageStatus.ACTIVE)
                                .tag(null)
                                .isBookmarked(false)
                                .placeDetail(null)
                                .build()
                )
                .createdAt(message.getCreatedAt())
                .build();
    }

    // 장소 공유
    @Transactional
    public MessageResponse sendMapMessageToRoom(Long userId, Long roomId, String placeId, String displayName, Double latitude, Double longitude, String photoUrl) throws FirebaseMessagingException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방이 존재하지 않습니다."));

        Message message = Message.builder()
                .room(room)
                .user(user)
                .messageType(MessageType.MAP)
                .messageStatus(MessageStatus.ACTIVE)
                .content(null)
                .build();
        messageRepository.save(message);

        MessagePlaceDetail messagePlaceDetail = MessagePlaceDetail.builder()
                .message(message)
                .placeId(placeId)
                .displayName(displayName)
                .latitude(latitude)
                .longitude(longitude)
                .photoUrl(photoUrl)
                .build();
        messagePlaceDetailRepository.save(messagePlaceDetail);

        message.setContent(messagePlaceDetail.getDisplayName());
        messageRepository.save(message);

        sendMessagePushNotification(roomId, message);

        return MessageResponse.builder()
                .actionType(ActionType.NEW_MAP_MESSAGE)
                .user(new UserResponse(userId, user.getNickname(), user.getPhotoUrl()))
                .message(
                        MessageDto.builder()
                                .messageId(message.getMessageId())
                                .content(message.getContent())
                                .messageType(message.getMessageType())
                                .messageStatus(MessageStatus.ACTIVE)
                                .tag(null)
                                .isBookmarked(false)
                                .placeDetail(makePlaceDetail(message.getMessageId()))
                                .build()
                )
                .createdAt(message.getCreatedAt())
                .build();
    }

    // 북마크 설정
    @Transactional
    public MessageResponse setBookmark(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("해당 메세지가 존재하지 않습니다."));

        MessageDto messageDto = MessageDto.builder()
                .messageId(messageId)
                .content(null)
                .messageType(null)
                .messageStatus(null)
                .tag(null)
                .placeDetail(null)
                .build();

        MessageBookmark messageBookmark = messageBookmarkRepository.findByMessage_MessageId(messageId);

        if (messageBookmark == null) {
            MessageBookmark bookmark = MessageBookmark.builder()
                    .room(message.getRoom())
                    .message(message)
                    .content(message.getContent())
                    .build();
            messageBookmarkRepository.save(bookmark);
            messageDto.setIsBookmarked(true);
        } else {
            messageBookmarkRepository.delete(messageBookmark);
            messageDto.setIsBookmarked(false);
        }

        return MessageResponse.builder()
                .actionType(ActionType.BOOKMARK_UPDATE)
                .user(null)
                .message(messageDto)
                .createdAt(null)
                .build();
    }

    // 장소 태그 설정
    @Transactional
    public MessageResponse setPlaceTag(Long messageId, PlaceTag placeTag) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("해당 메세지가 존재하지 않습니다."));

        if (message.getMessageType().equals(MessageType.TEXT)) {
            throw new IllegalArgumentException("해당 타입의 메세지에는 장소 태그를 지정할 수 없습니다.");
        }

        // todo messagePlaceDetail에서 조회하기?

        MessageDto messageDto = MessageDto.builder()
                .messageId(messageId)
                .content(null)
                .messageType(null)
                .messageStatus(null)
                .isBookmarked(null)
                .placeDetail(null)
                .build();

        MessagePlace messagePlace = messagePlaceRepository.findByMessage_MessageId(messageId);

        if (messagePlace == null) {
            MessagePlace m = MessagePlace.builder()
                    .room(message.getRoom())
                    .message(message)
                    .placeTag(placeTag)
                    .build();
            messagePlaceRepository.save(m);
            messageDto.setTag(placeTag);
        } else if (!messagePlace.getPlaceTag().equals(placeTag)) {
            messagePlace.setPlaceTag(placeTag);
            messagePlaceRepository.save(messagePlace);
            messageDto.setTag(placeTag);
        } else {
            messagePlaceRepository.delete(messagePlace);
            messageDto.setTag(null);
        }

        return MessageResponse.builder()
                .actionType(ActionType.TAG_UPDATE)
                .user(null)
                .message(messageDto)
                .createdAt(null)
                .build();
    }

    // 메세지 수정 -> content랑 messageStatus만 조회
    @Transactional
    public MessageResponse editMessage(Long messageId, String content) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("해당 메세지가 존재하지 않습니다."));

        if (content == null) {
            throw new IllegalArgumentException("메세지가 비어있습니다.");
        }

        message.setMessageStatus(MessageStatus.EDIT);
        message.setContent(content);
        message.setUpdatedAt(LocalDateTime.now());
        messageRepository.save(message);

        MessageDto messageDto = MessageDto.builder()
                .messageId(messageId)
                .content(content)
                .messageType(null)
                .messageStatus(MessageStatus.EDIT)
                .tag(null)
                .isBookmarked(null)
                .placeDetail(null)
                .build();

        return MessageResponse.builder()
                .actionType(ActionType.MESSAGE_EDIT)
                .user(null)
                .message(messageDto)
                .createdAt(null)
                .build();
    }

    // 메세지 삭제 -> messageId랑 messageStatus만 조회
    @Transactional
    public MessageResponse deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("해당 메세지가 존재하지 않습니다."));

        message.setMessageStatus(MessageStatus.DELETE);
        message.setUpdatedAt(LocalDateTime.now());
        messageRepository.save(message);

        MessageDto messageDto = MessageDto.builder()
                .messageId(messageId)
                .content(null)
                .messageType(null)
                .messageStatus(MessageStatus.DELETE)
                .tag(null)
                .isBookmarked(null)
                .placeDetail(null)
                .build();

        return MessageResponse.builder()
                .actionType(ActionType.MESSAGE_DELETE)
                .user(null)
                .message(messageDto)
                .createdAt(null)
                .build();
    }
//   //todo 채팅방 구독상태인가
//    private boolean isUserConnected(Room room, Long userId) {
//
//    }
//
    private void sendMessagePushNotification(Long roomId, Message message) throws FirebaseMessagingException {
        List<User> users = userRoomRepository.findUsersByRoomIdAndIsAlarm(roomId, IsAlarm.ON);
        log.info("message push notification send");

//        Room room = roomRepository.findById(roomId)
//                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방이 존재하지 않습니다"));
//
//        List<Token> tokens = users.stream()
//                .filter(user -> !isUserConnected(room, user.getUserId()))
//                .map(Token::getUser)
//                .filter(token -> token != null)
//                .;
//
//        User user = message.getUser();
//        String roomName = room.getRoomName();
//        String content = user.getNickname() + "\n" +
//                (message.getMessageType().equals(MessageType.TEXT) ?
//                message.getContent()
//                : messagePlaceDetailRepository.findByMessage_MessageId(message.getMessageId()).getDisplayName());
//        String image = user.getPhotoUrl();
//
//        for (Token t : tokens) {
//            FcmSendRequest fcmSendRequest = FcmSendRequest.builder()
//                    .requestType(RequestType.MESSAGE)
//                    .id(0L)
//                    .title(roomName)
//                    .content(content)
//                    .image(image)
//                    .token(t.getToken())
//                    .build();
//
//            fcmSender.sendPushNotification(fcmSendRequest);
//        }
    }

    private PlaceDetail makePlaceDetail(Long messageId) {
        MessagePlaceDetail mpd = messagePlaceDetailRepository.findByMessage_MessageId(messageId);

        if (mpd == null) {
            return null;
        }
        return PlaceDetail.builder()
                .placeId(mpd.getPlaceId())
                .displayName(mpd.getDisplayName())
                .latitude(mpd.getLatitude())
                .longitude(mpd.getLongitude())
                .photoUrl(mpd.getPhotoUrl())
                .build();
    }
}
