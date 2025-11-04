package triB.triB.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triB.triB.chat.dto.RoomChatResponse;
import triB.triB.chat.service.ChatService;
import triB.triB.global.response.ApiResponse;
import triB.triB.global.security.UserPrincipal;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    // 채팅방 조회시 모든 채팅 내용 가져오기
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomChatResponse>> getMessages(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable(name = "roomId") Long roomId) {
        Long userId = userPrincipal.getUserId();
        RoomChatResponse result = chatService.getRoomMessages(userId, roomId);
        return ApiResponse.ok("이전까지의 모든 채팅 내역을 조회했습니다.", result);
    }

    // 일정 생성하기

    // 생성된 일정 조회하기


}
