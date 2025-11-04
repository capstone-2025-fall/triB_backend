package triB.triB.room.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import triB.triB.room.dto.*;
import triB.triB.room.service.RoomService;
import triB.triB.global.response.ApiResponse;
import triB.triB.global.security.UserPrincipal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<RoomsResponse>>>> getRooms(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getUserId();
        Map<String, List<RoomsResponse>> rooms = new HashMap<>();
        List<RoomsResponse> roomList = roomService.getRoomList(userId);
        rooms.put("rooms", roomList);
        return ApiResponse.ok("유저의 채팅방 목록을 조회했습니다.", rooms);
    }

    @GetMapping("/content")
    public ResponseEntity<ApiResponse<Map<String, List<RoomsResponse>>>> searchRooms(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String content
    ) {
        Long userId = userPrincipal.getUserId();
        Map<String, List<RoomsResponse>> rooms = new HashMap<>();
        List<RoomsResponse> roomList = roomService.searchRoomList(userId, content);
        rooms.put("rooms", roomList);
        return ApiResponse.ok("유저의 채팅방 목록을 조회했습니다.", rooms);
    }

//    @GetMapping("/country")
//    public ResponseEntity<ApiResponse<List<String>>> searchCountries(@RequestParam String country) {
//        List<String> countries = roomService.getCountries(country);
//        return ApiResponse.ok("나라를 검색했습니다.", countries);
//    }

    @PostMapping("/users/summary")
    public ResponseEntity<ApiResponse<List<ChatUserResponse>>> getCreateUsers(@RequestBody UserRequest userRequest) {
        List<ChatUserResponse> users = roomService.selectFriends(userRequest.getUserIds());
        return ApiResponse.ok("유저 프로필을 불러왔습니다.", users);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody RoomRequest roomRequest
    ){
        Long userId = userPrincipal.getUserId();
        RoomResponse room = roomService.makeChatRoom(userId, roomRequest);
        return ApiResponse.created("채팅방을 생성했습니다.", room);
    }

    // todo 채팅방 나가기

    // todo 채팅방 수정하기
}
