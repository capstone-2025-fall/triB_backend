package triB.triB.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponse;
import triB.triB.chat.dto.*;
import triB.triB.chat.service.SocketService;
import triB.triB.global.response.ApiResponse;
import triB.triB.global.security.JwtProvider;
import triB.triB.global.security.UserPrincipal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SocketController {

    private final SocketService socketService;
    private final JwtProvider jwtProvider;

    @MessageMapping("/chat/{roomId}/send") // client가 메세지 전송
    @SendTo("/sub/chat/{roomId}")
    public ApiResponse<MessageResponse> sendMessage(
            @Header("Authorization") String authHeader,
            @DestinationVariable Long roomId,
            @Payload MessageContentRequest messageContentRequest)
    {
        String token = authHeader.substring(7); // "Bearer " 제거
        Long userId = jwtProvider.extractUserId(token);
        MessageResponse result = socketService.sendMessageToRoom(userId, roomId, messageContentRequest.getContent());
        return ApiResponse.success("메세지를 전송했습니다.", result);
    }

    // 지도에서 장소 공유하기
    @MessageMapping("/chat/{roomId}/map/send")
    @SendTo("/sub/chat/{roomId}")
    public ApiResponse<MessageResponse> sendMapMessage(
            @Header("Authorization") String authHeader,
            @DestinationVariable Long roomId,
            @Payload PlaceRequest placeRequest
    )
    {
        String token = authHeader.substring(7); // "Bearer " 제거
        Long userId = jwtProvider.extractUserId(token);
        MessageResponse result = socketService.sendMapMessageToRoom(userId, roomId, placeRequest.getPlaceId(), placeRequest.getDisplayName(), placeRequest.getLatitude(), placeRequest.getLongitude(), placeRequest.getPhotoUrl());
        return ApiResponse.success("장소를 공유했습니다.", result);
    }

    // 북마크 설정하는거 실시간으로 보여야됨
    @MessageMapping("/chat/{roomId}/bookmark")
    @SendTo("/sub/chat/{roomId}")
    public ApiResponse<MessageResponse> setMessageBookmark(
            @DestinationVariable Long roomId,
            @Payload MessageIdRequest messageIdRequest)
    {
        MessageResponse result = socketService.setBookmark(messageIdRequest.getMessageId());
        return ApiResponse.success("북마크를 업데이트했습니다.", result);
    }

    // 태그 설정하는거 실시간으로 보여야됨
    // 위경도 이름 이런건 어디다 저장하지?
    @MessageMapping("/chat/{roomId}/tag")
    @SendTo("/sub/chat/{roomId}")
    public ApiResponse<MessageResponse> setMessageTag(
            @DestinationVariable Long roomId,
            @Payload MessagePlaceRequest messagePlaceRequest)
    {
        MessageResponse result = socketService.setPlaceTag(messagePlaceRequest.getMessageId(), messagePlaceRequest.getPlaceTag());
        return ApiResponse.success("장소 태그를 업데이트했습니다.", result);
    }

    // 메세지 수정한거 실시간으로 보여야됨
    @MessageMapping("/chat/{roomId}/edit")
    @SendTo("/sub/chat/{roomId}")
    public ApiResponse<MessageResponse> editMessage(
            @DestinationVariable Long roomId,
            @Payload MessageEditRequest messageEditRequest)
    {
        MessageResponse result = socketService.editMessage(messageEditRequest.getMessageId(), messageEditRequest.getContent());
        return ApiResponse.success("메세지를 수정했습니다.", result);
    }

    // 메세지 삭제한 것도 실시간으로 보여야됨
    @MessageMapping("/chat/{roomId}/delete")
    @SendTo("/sub/chat/{roomId}")
    public ApiResponse<MessageResponse> deleteMessage(
            @DestinationVariable Long roomId,
            @Payload Long messageId)
    {
        MessageResponse result = socketService.deleteMessage(messageId);
        return ApiResponse.success("메세지를 삭제했습니다.", result);
    }

    // 일정 생성하기

}