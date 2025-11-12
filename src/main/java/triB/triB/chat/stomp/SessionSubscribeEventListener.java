package triB.triB.chat.stomp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.DefaultSimpUserRegistry;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import triB.triB.global.security.UserPrincipal;

import java.security.Principal;

@Component
@Slf4j
@AllArgsConstructor
public class SessionSubscribeEventListener {

    private final SimpUserRegistry simpUserRegistry;

    @EventListener(SessionSubscribeEvent.class)
    public void handleSubscribeEvent(SessionSubscribeEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Authentication auth = (Authentication) accessor.getUser();
        if (auth == null)
            return;
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        Long userId = userPrincipal.getUserId();

        String subscriptionId = accessor.getSubscriptionId();
        Long roomId = (Long) accessor.getSessionAttributes().get("subscription:" + subscriptionId);

        log.debug("채팅방 구독 이벤트 처리 완료: userId={}, roomId={}, subscriptionId={}", userId, roomId, subscriptionId);
    }
}
