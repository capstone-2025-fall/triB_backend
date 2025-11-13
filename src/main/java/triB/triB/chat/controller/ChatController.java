package triB.triB.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import triB.triB.chat.dto.RoomChatResponse;
import triB.triB.chat.dto.TripCreateStatusResponse;
import triB.triB.chat.dto.TripResponse;
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
    @PostMapping("/trip")
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "roomId") Long roomId
    ) {
        Long userId = userPrincipal.getUserId();
        TripResponse response = new TripResponse(chatService.makeTrip(userId, roomId).block());
        return ApiResponse.created("일정을 생성했습니다.", response);
    }

    // 일정 생성 상태 조회
    @GetMapping("/trip/status")
    public ResponseEntity<ApiResponse<TripCreateStatusResponse>> tripStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "roomId") Long roomId
    ){
        Long userId = userPrincipal.getUserId();
        TripCreateStatusResponse response = chatService.getTripStatus(userId, roomId);
        return ApiResponse.ok("일정 생성 상태를 조회했습니다.", response);
    }

}