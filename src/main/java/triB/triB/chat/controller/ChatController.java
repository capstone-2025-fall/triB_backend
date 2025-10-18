package triB.triB.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import triB.triB.chat.dto.MessageResponse;
import triB.triB.chat.service.SocketService;
import triB.triB.global.security.UserPrincipal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SocketService socketService;

    @MessageMapping("/chat.{roomId}")
    @SendTo("/sub/chat.{roomId}")
    public MessageResponse sendMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @DestinationVariable Long roomId,
            @Payload String content)
    {
        Long userId = userPrincipal.getUserId();
        return socketService.sendMessageToRoom(userId, roomId, content);
    }
}
