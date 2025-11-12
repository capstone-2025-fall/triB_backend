package triB.triB.chat.stomp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import triB.triB.chat.service.SocketService;
import triB.triB.global.security.UserPrincipal;

@Component
@Slf4j
@AllArgsConstructor
public class SessionUnsubscribeEventListener {

    private final SocketService socketService;

    @EventListener(SessionUnsubscribeEvent.class)
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Authentication auth = (Authentication) accessor.getUser();
        if (auth == null)
            return;
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        Long userId = userPrincipal.getUserId();

        String subscriptionId = accessor.getSubscriptionId();
        Long roomId = (Long) accessor.getSessionAttributes().get("subscription:" + subscriptionId);

        if (roomId != null){
            try {
                socketService.saveLastReadMessage(userId, roomId);
            } catch (Exception e) {
                log.error("마지막 읽은 메시지 저장 실패: userId={}, roomId={}", userId, roomId, e);

            }
        }
    }
}
