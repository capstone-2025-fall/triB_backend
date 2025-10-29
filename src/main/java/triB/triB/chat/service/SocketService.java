//package triB.triB.chat.service;
//
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import triB.triB.auth.entity.Token;
//import triB.triB.auth.entity.User;
//import triB.triB.auth.repository.TokenRepository;
//import triB.triB.auth.repository.UserRepository;
//import triB.triB.chat.dto.MessageResponse;
//import triB.triB.chat.entity.Message;
//import triB.triB.chat.entity.MessageStatus;
//import triB.triB.chat.entity.MessageType;
//import triB.triB.chat.repository.MessageRepository;
//import triB.triB.global.fcm.FcmSendRequest;
//import triB.triB.global.fcm.FcmSender;
//import triB.triB.global.fcm.RequestType;
//import triB.triB.room.entity.Room;
//import triB.triB.room.entity.UserRoom;
//import triB.triB.room.repository.RoomRepository;
//import triB.triB.room.repository.UserRoomRepository;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class SocketService {
//
//    private final MessageRepository messageRepository;
//    private final RoomRepository roomRepository;
//    private final UserRepository userRepository;
//    private final UserRoomRepository userRoomRepository;
//    private final TokenRepository tokenRepository;
//    private final FcmSender fcmSender;
//
//    @Transactional
//    public MessageResponse sendMessageToRoom(Long userId, Long roomId, String content){
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
//
//        Room room = roomRepository.findById(roomId)
//                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방이 존재하지 않습니다."));
//
//        Message message = Message.builder()
//                .room(room)
//                .user(user)
//                .messageType(MessageType.TEXT)
//                .messageStatus(MessageStatus.ACTIVE)
//                .content(content)
//                .build();
//        messageRepository.save(message);
//
//        sendMessagePushNotification(roomId, message);
//
//        return null;
//    }
//
//    private boolean isUserConnected(Long userId) {
//
//    }
//
//    private void sendMessagePushNotification(Long roomId, Message message){
//        List<User> users = userRoomRepository.findUsersByRoomId(roomId);
//        List<Token> tokens = users.stream()
//                .filter(user -> !isUserConnected(user.getUserId()))
//                .map(Token::getUser)
//                .filter(token -> token != null)
//                .
//        for (Token t : token) {
//            FcmSendRequest fcmSendRequest = FcmSendRequest.builder()
//                    .requestType(RequestType.FRIEND_REQUEST)
//                    .id(0L)
//                    .title("TriB")
//                    .content(requester.getNickname()+" 님이 나에게 친구를 신청했어요!")
//                    .image(tribImage)
//                    .token(t.getToken())
//                    .build();
//
//            fcmSender.sendPushNotification(fcmSendRequest);
//        }
//    }
//}
