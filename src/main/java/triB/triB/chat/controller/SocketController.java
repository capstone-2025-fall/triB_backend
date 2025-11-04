package triB.triB.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import triB.triB.chat.dto.MessageEditRequest;
import triB.triB.chat.dto.MessagePlaceRequest;
import triB.triB.chat.dto.MessageResponse;
import triB.triB.chat.service.SocketService;
import triB.triB.global.security.UserPrincipal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SocketService socketService;

    @MessageMapping("/chat/{roomId}/send")
    @SendTo("/sub/chat/{roomId}")
    public MessageResponse sendMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @DestinationVariable Long roomId,
            @Payload String content)
    {
        Long userId = userPrincipal.getUserId();
        return socketService.sendMessageToRoom(userId, roomId, content);
    }

    // 북마크 설정하는거 실시간으로 보여야됨
    @MessageMapping("/chat/{roomId}/bookmark")
    @SendTo("/sub/chat/{roomId}")
    public MessageResponse setMessageBookmark(
            @DestinationVariable Long roomId,
            @Payload Long messageId)
    {
        return socketService.setBookmark(roomId, messageId);
    }

    // 태그 설정하는거 실시간으로 보여야됨
    @MessageMapping("/chat/{roomId}/tag")
    @SendTo("/sub/chat/{roomId}")
    public MessageResponse setMessageTag(
            @DestinationVariable Long roomId,
            @Payload MessagePlaceRequest messagePlaceRequest)
    {
        return socketService.setPlaceTag(messagePlaceRequest.getMessageId(), messagePlaceRequest.getPlaceTag());
    }

    // 메세지 수정한거 실시간으로 보여야됨
    @MessageMapping("/chat/{roomId}/edit")
    @SendTo("/sub/chat/{roomId}")
    public MessageResponse editMessage(
            @DestinationVariable Long roomId,
            @Payload MessageEditRequest messageEditRequest)
    {
        return socketService.editMessage(messageEditRequest.getMessageId(), messageEditRequest.getContent());
    }

    // 메세지 삭제한 것도 실시간으로 보여야됨
    @MessageMapping("/chat/{roomId}/delete")
    @SendTo("/sub/chat/{roomId}")
    public MessageResponse deleteMessage(
            @DestinationVariable Long roomId,
            @Payload Long messageId)
    {
        return socketService.deleteMessage(messageId);
    }

    // 지도에서 장소 공유하기

}
