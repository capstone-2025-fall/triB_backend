package triB.triB.auth.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import triB.triB.auth.dto.*;
import triB.triB.auth.service.AuthService;
import triB.triB.auth.service.MailService;
import triB.triB.global.response.ApiResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 이메일로 인증번호 보내기
     */
    @PostMapping("/email/{email}")
    public ResponseEntity<ApiResponse<Void>> sendEmailCode(@PathVariable("email") @Email String email) {
        authService.sendCodeToEmail(email);
        return ApiResponse.ok("인증번호를 보냈습니다.", null);
    }

    /**
     * 이메일로 보낸 인증번호 확인
     */
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestBody EmailRequest emailRequest) {
        authService.verifiedCode(emailRequest.getEmail(), emailRequest.getCode());
        return ApiResponse.ok("인증번호가 일치합니다.", null);
    }

    /**
     * 아이디 중복 확인
     */
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Void>> checkUsername(@RequestParam("username") String username) {
        authService.duplicateUsername(username);
        return ApiResponse.ok("사용 가능한 아이디입니다.", null);
    }

    /**
     * 이메일로 회원가입
     */
    @PostMapping(value = "/email",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> signup(
            @RequestPart("photo") MultipartFile photo,
            @RequestPart("meta") AuthRequest authRequest
    ) {
        authService.signup(photo, authRequest);
        return ApiResponse.created("회원가입 성공", null);
    }

    /**
     * 이메일로 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response =  authService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        return ApiResponse.ok("로그인 성공", response);
    }

    /**
     * 신규 소셜로그인 유저 회원가입 & 로그인
     */
    @PostMapping(value = "/complete-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AuthResponse>> completeProfile(
            @RequestPart("photo") MultipartFile photo,
            @RequestPart("meta") RegisterRequest registerRequest
    ) {
        Long userId = authService.socialSignup(photo, registerRequest);
        AuthResponse response = authService.socialLogin(userId);
        return ApiResponse.created("소셜로그인 회원가입을 성공했습니다.", response);
    }
}



