package triB.triB.friendship.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import triB.triB.friendship.dto.NewUserResponse;
import triB.triB.friendship.dto.UserResponse;
import triB.triB.friendship.service.FriendshipService;
import triB.triB.global.response.ApiResponse;
import triB.triB.global.security.UserPrincipal;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> listMyFriends(@AuthenticationPrincipal UserPrincipal UserPrincipal){
        Long userId = UserPrincipal.getUserId();
        List<UserResponse> responses = friendshipService.getMyFriends(userId);
        return ApiResponse.ok("유저의 친구 목록을 조회했습니다.", responses);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> myProfile(@AuthenticationPrincipal UserPrincipal UserPrincipal){
        Long userId = UserPrincipal.getUserId();
        UserResponse response = friendshipService.getMyProfile(userId);
        return ApiResponse.ok("유저의 프로필을 조회했습니다.", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchFriends(
            @AuthenticationPrincipal UserPrincipal UserPrincipal,
            @RequestParam(name = "nickname") String nickname
    ){
        Long userId = UserPrincipal.getUserId();
        List<UserResponse> responses = friendshipService.searchMyFriends(userId, nickname);
        return ApiResponse.ok("친구를 검색했습니다.", responses);
    }

    @GetMapping("/me/username")
    public ResponseEntity<ApiResponse<Map<String, Object>>> myUsername(@AuthenticationPrincipal UserPrincipal UserPrincipal){
        Long userId = UserPrincipal.getUserId();
        Map<String, Object> response = friendshipService.getMyUsername(userId);
        return ApiResponse.ok("유저의 아이디를 조회했습니다.", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<NewUserResponse>> searchNewFriend(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "username") String username
    ){
        Long userId = userPrincipal.getUserId();
        NewUserResponse response = friendshipService.searchNewFriend(userId, username);
        return ApiResponse.ok("해당 아이디의 유저를 조회했습니다.", response);
    }
}
