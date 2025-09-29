package triB.triB.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import triB.triB.auth.entity.IsAlarm;
import triB.triB.auth.entity.User;
import triB.triB.auth.entity.UserStatus;
import triB.triB.auth.repository.UserRepository;
import triB.triB.global.infra.AwsS3Client;
import triB.triB.global.infra.RedisClient;
import triB.triB.user.dto.MyProfile;

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
    public void updateMyProfile(Long userId, MultipartFile photo, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        if (photo != null && !photo.isEmpty()){
            log.info("userId = {} 의 프로필 이미지를 변경합니다.", userId);
            String newPhoto = s3Client.uploadFile(photo);
            user.setPhotoUrl(newPhoto);
        }
        if (nickname != null) {
            log.info("userId = {} 의 닉네임을 변경합니다.", userId);
            user.setNickname(nickname);
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
        redisClient.deleteData("rf", String.valueOf(userId));
        s3Client.delete(user.getPhotoUrl());
        user.setUserStatus(UserStatus.DELETED);
        user.setEmail(null);
        user.setUsername(null);
        userRepository.save(user);
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
}
