package triB.triB.global.stomp;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.UserRepository;
import triB.triB.chat.repository.MessageRepository;
import triB.triB.global.security.JwtProvider;
import triB.triB.global.security.UserPrincipal;
import triB.triB.room.entity.Room;
import triB.triB.room.entity.RoomReadState;
import triB.triB.room.entity.RoomReadStateId;
import triB.triB.room.repository.RoomReadStateRepository;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;
    private final RoomReadStateRepository roomReadStateRepository;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try{
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

            // WebSocket 연결 요청
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String jwtToken = accessor.getFirstNativeHeader("Authorization");

                if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
                    throw new IllegalArgumentException("인증 토큰이 필요합니다.");
                }

                String token = jwtToken.substring(7);

                if (!jwtProvider.validateAccessToken(token)) {
                    throw new JwtException("유효하지 않는 토큰입니다.");
                }

                Long userId = jwtProvider.extractUserId(token);
                log.info("WebSocket 연결 성공: userId= {}", userId);
            }
            // todo 채팅룸 구독 요청
            else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

                String destination = accessor.getDestination();

                log.info("채팅룸 확인전");
                if (destination == null || !destination.startsWith("/sub/chat/"))
                    throw new IllegalArgumentException("잘못된 요청입니다.");

                log.info("채팅룸 확인 중");

                try {
                    Long roomId = Long.parseLong(destination.substring("/sub/chat/".length()));
                    log.info("roomID={}", roomId);

                    String jwtToken = accessor.getFirstNativeHeader("Authorization");
                    if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
                        throw new BadCredentialsException("인증 토큰이 필요합니다.");
                    }

                    String token = jwtToken.substring(7);
                    if (!jwtProvider.validateAccessToken(token)) {
                        throw new JwtException("유효하지 않는 토큰입니다.");
                    }
                    Long userId = jwtProvider.extractUserId(token);

                    log.info("SUBSCRIBE - subscriptionId= {}, userId={}, roomId={}", accessor.getSubscriptionId(), userId, roomId);

                    boolean hasAccess = userRoomRepository.existsByUser_UserIdAndRoom_RoomId(userId, roomId);

                    if (!hasAccess) {
                        log.warn("채팅방 구독 권한 없음. userId={}, roomId={}", userId, roomId);
                        throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");
                    }

                    // 구독 정보 저장 (UNSUBSCRIBE 시 사용)
                    accessor.getSessionAttributes().put("subscription:" + accessor.getSubscriptionId(), roomId);

                    log.info("채팅방 구독 성공. userId={}, roomId={}", userId, roomId);
                } catch (NumberFormatException e) {
                    log.error("잘못된 roomId 형식: {}", destination);
                    throw new IllegalArgumentException("잘못된 채팅방 ID 형식입니다.");
                }
            }
            else if (StompCommand.SEND.equals(accessor.getCommand())) {
                String jwtToken = accessor.getFirstNativeHeader("Authorization");
                if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
                    throw new BadCredentialsException("인증 토큰이 필요합니다.");
                }

                String token = jwtToken.substring(7);
                if (!jwtProvider.validateAccessToken(token)) {
                    throw new JwtException("유효하지 않는 토큰입니다.");
                }
                Long userId = jwtProvider.extractUserId(token);

                String destination = accessor.getDestination();

                log.info("메시지 전송 요청 확인: destination={}", destination);
                if (destination == null || !destination.startsWith("/pub/chat/"))
                    throw new IllegalArgumentException("잘못된 요청입니다.");

                // /pub/chat/{roomId}/send 또는 /pub/chat/{roomId} 형식에서 roomId 추출
                String pathAfterPrefix = destination.substring("/pub/chat/".length());
                String roomIdStr = pathAfterPrefix.contains("/")
                        ? pathAfterPrefix.substring(0, pathAfterPrefix.indexOf("/"))
                        : pathAfterPrefix;
                Long roomId = Long.parseLong(roomIdStr);

                log.info("SEND - userId={}, roomId={}", userId, roomId);

                boolean hasAccess = userRoomRepository.existsByUser_UserIdAndRoom_RoomId(userId, roomId);

                if (!hasAccess) {
                    log.warn("채팅방 전송 권한 없음. userId={}, roomId={}", userId, roomId);
                    throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");
                }

                log.info("메시지 전송 권한 확인 완료. userId={}, roomId={}", userId, roomId);
            }
            // 채팅룸 구독 해제
            else if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {

                log.info("구독 해제 요청 시작");
                String subscriptionId = accessor.getSubscriptionId();
                Long roomId = (Long) accessor.getSessionAttributes().get("subscription:"+subscriptionId);
                log.info("roomId={}", roomId);

                if (roomId == null)
                    throw new IllegalArgumentException("구독 정보가 없습니다.");

                String jwtToken = accessor.getFirstNativeHeader("Authorization");
                if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
                    throw new BadCredentialsException("인증 토큰이 필요합니다.");
                }

                String token = jwtToken.substring(7);
                if (!jwtProvider.validateAccessToken(token)) {
                    throw new JwtException("유효하지 않는 토큰입니다.");
                }
                Long userId = jwtProvider.extractUserId(token);

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new EntityNotFoundException("해당 채팅방이 존재하지 않습니다."));

                Long messageId = messageRepository.findLastReadMessageIdByRoom_RoomId(roomId);

                if (messageId == null) {
                    accessor.getSessionAttributes().remove("subscription:" + subscriptionId);
                    return message;
                }

                RoomReadState r = roomReadStateRepository.findByRoom_RoomIdAndUser_UserId(roomId, userId);

                log.info("마지막으로 읽은 메세지 저장");
                // 만약에 이미 읽은 기록이 있다면 업데이트 없다면 생성
                if (r == null) {
                    RoomReadStateId id = new RoomReadStateId(userId, roomId);
                    RoomReadState roomReadState = RoomReadState.builder()
                            .id(id)
                            .user(user)
                            .room(room)
                            .lastReadMessageId(messageId)
                            .build();
                    roomReadStateRepository.save(roomReadState);
                } else {
                    r.setLastReadMessageId(messageId);
                    roomReadStateRepository.save(r);
                }
                accessor.getSessionAttributes().remove("subscription:"+subscriptionId);
                log.info("채팅방 구독 해제: userId={}, roomId={}", userId, roomId);
            }
            // 앱 종료시 웹소켓 연결 종료
            else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                SecurityContextHolder.clearContext();
            }
            return message;
        } catch (Exception e) {
            log.error("STOMP 메시지 처리 중 에러 발생: {}", e.getMessage(), e);
            return null;  // 메시지 처리 중단

        }
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        ChannelInterceptor.super.postSend(message, channel, sent);
    }

    private void validateTokenOnConnect(StompHeaderAccessor accessor) {
        if(StompCommand.CONNECT.equals(accessor.getCommand())){
            String accessToken = Objects.requireNonNull(
                    accessor.getFirstNativeHeader("Authorization"));

            if(!jwtProvider.validateAccessToken(accessToken)){
                throw new JwtException("JWT 토큰이 옳지 않습니다.");
            }
        }
    }
}