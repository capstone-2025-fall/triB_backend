package triB.triB.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.services.s3.S3Client;
import triB.triB.auth.dto.UnlinkResponse;
import triB.triB.auth.entity.*;
import triB.triB.auth.repository.OauthAccountRepository;
import triB.triB.auth.repository.TokenRepository;
import triB.triB.auth.repository.UserRepository;
import triB.triB.global.infra.AwsS3Client;
import triB.triB.global.infra.RedisClient;
import triB.triB.user.dto.MyProfile;
import triB.triB.user.dto.UpdateProfileRequest;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AwsS3Client s3Client;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisClient redisClient;
    private final TokenRepository tokenRepository;
    private final OauthAccountRepository oauthAccountRepository;
    private final @Qualifier("kakaoWebClient") WebClient kakaoWebClient;

    public MyProfile getMyProfile(Long userId) {
        log.info("userId = {}의 프로필", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        return new MyProfile(
                user.getNickname(),
                user.getUsername(),
                user.getPhotoUrl(),
                user.getIsAlarm()
        );
    }

    @Transactional
    public void updateMyProfile(Long userId, MultipartFile photo, UpdateProfileRequest updateProfileRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        if (updateProfileRequest != null){
            if (Boolean.TRUE.equals(updateProfileRequest.getIsDeleted())) {
                if (user.getPhotoUrl() != null) {
                    s3Client.delete(user.getPhotoUrl());
                }
                user.setPhotoUrl(null);
            }
            String nickname = updateProfileRequest.getNickname();
            if (nickname != null && !nickname.isEmpty()) {
                log.info("userId = {} 의 닉네임을 변경합니다.", userId);
                user.setNickname(nickname);
            }
        }
        if (photo != null && !photo.isEmpty()){
            log.info("userId = {} 의 프로필 이미지를 변경합니다.", userId);
            if (user.getPhotoUrl() != null) {
                s3Client.delete(user.getPhotoUrl());
            }
            String newPhoto = s3Client.uploadFile(photo);
            user.setPhotoUrl(newPhoto);
        }
        userRepository.save(user);
    }

    @Transactional
    public IsAlarm updateAlarm(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        IsAlarm setting = user.getIsAlarm() == IsAlarm.ON ? IsAlarm.OFF : IsAlarm.ON;
        user.setIsAlarm(setting);
        userRepository.save(user);
        log.info("userId = {} 의 알람을 변경합니다.", userId);
        return user.getIsAlarm();
    }

    @Transactional
    public void checkPassword(Long userId, String password) {
        log.info("userId = {} 의 비밀번호를 확인합니다.", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())){
            throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
        }
    }

    @Transactional
    public void updatePassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        log.info("userId = {} 의 비밀번호를 변경합니다.", userId);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        OauthAccount account;
        if ((account = oauthAccountRepository.findByUser_UserId(userId)) != null) {
            log.info("oauth 존재 확인 완료");
            UnlinkResponse response;
            if (account.getProvider().equals("kakao")) {
                log.info("kakao와 연결 끊기");
                response = unlinkKakao(account.getProviderUserId());
                log.info("kakao와 연결 끊기 성공");
            }
            oauthAccountRepository.delete(account);
        }

        redisClient.deleteData("rf", String.valueOf(userId));
        s3Client.delete(user.getPhotoUrl());
        user.setUserStatus(UserStatus.DELETED);
        user.setNickname("(탈퇴한 사용자)");
        user.setEmail(null);
        user.setUsername(null);
        userRepository.save(user);
        log.info("user 상태변경 완료");

        log.info("userId = {} 인 유저가 탈퇴했습니다.", userId);
    }

    public Map<String, Object> getMyUsername(Long userId){
        Map<String, Object> map = new HashMap<>();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        String username = user.getUsername();
        map.put("username", username);
        return map;
    }

    @Transactional
    public void saveToken(Long userId, String token) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

            tokenRepository.findByUser_UserId(userId)
                    .ifPresentOrElse(
                            t -> {
                                t.setToken(token);
                                tokenRepository.save(t);
                            },
                            () -> {
                                Token t = Token.builder()
                                        .user(user)
                                        .token(token)
                                        .build();
                                tokenRepository.save(t);
                            }
                    );
        } catch (DataIntegrityViolationException e){
            tokenRepository.findByUser_UserId(userId)
                    .ifPresent(t -> t.setToken(token));
        }
    }

    @Transactional
    public void logout(Long userId) {
        tokenRepository.findByUser_UserId(userId)
                .ifPresent(tokenRepository::delete);
    }

    private UnlinkResponse unlinkKakao(String providerUserId){
        return kakaoWebClient.post()
                .uri("/v1/user/unlink")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("target_id_type", "user_id")
                        .with("target_id", providerUserId))
                .retrieve()
                .bodyToMono(UnlinkResponse.class)
                .block();
    }
}
