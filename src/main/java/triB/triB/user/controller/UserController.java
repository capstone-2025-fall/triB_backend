package triB.triB.user.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import triB.triB.auth.dto.AuthRequest;
import triB.triB.auth.entity.IsAlarm;
import triB.triB.auth.entity.User;
import triB.triB.global.response.ApiResponse;
import triB.triB.global.security.UserPrincipal;
import triB.triB.user.dto.MyProfile;
import triB.triB.user.dto.PasswordRequest;
import triB.triB.user.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfile>> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getUserId();
        MyProfile response = userService.getMyProfile(userId);
        return ApiResponse.ok("유저의 프로필을 조회했습니다.", response);
    }

    @PatchMapping(value = "/me",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MyProfile>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "nickname", required = false) String nickname
    ) {
        Long userId = userPrincipal.getUserId();
        userService.updateMyProfile(userId, photo, nickname);
        return ApiResponse.ok("프로필 이미지 및 닉네임을 수정했습니다.", null);
    }

    @PostMapping("/me/alarm")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateAlarm(@AuthenticationPrincipal UserPrincipal userPrincipal){
        Long userId = userPrincipal.getUserId();
        IsAlarm response = userService.updateAlarm(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("isAlarm", response.toString());
        return ApiResponse.ok("알람 상태를 바꿨습니다.", map);
    }

    @PostMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> confirmPassword(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody PasswordRequest passwordRequest) {
        Long userId = userPrincipal.getUserId();
        userService.checkPassword(userId, passwordRequest.getPassword());
        return ApiResponse.ok("비밀번호가 일치합니다.", null);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody PasswordRequest passwordRequest) {
        Long userId = userPrincipal.getUserId();
        userService.updatePassword(userId, passwordRequest.getPassword());
        return ApiResponse.ok("비밀번호를 수정했습니다.", null);
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getUserId();
        userService.deleteUser(userId);
        return ApiResponse.ok("성공적으로 탈퇴했습니다.", null);
    }
}
