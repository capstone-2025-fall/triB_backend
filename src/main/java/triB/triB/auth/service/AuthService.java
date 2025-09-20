package triB.triB.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import triB.triB.auth.dto.AuthRequest;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.UserRepository;
import triB.triB.global.infra.RedisClient;
import triB.triB.global.infra.AwsS3Client;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    @Value("${spring.mail.auth-code-expiration-millis}")
    private long authCodeExpirationMillis;

    private final MailService mailService;
    private final UserRepository userRepository;
    private final RedisClient redisClient;
    private final AwsS3Client s3Client;
    private static final String charPool = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    public void sendCodeToEmail(String email) {
        if (userRepository.existsByEmail(email))
            throw new DataIntegrityViolationException("이미 사용 중인 이메일입니다.");
        String title = "TriB 회원가입 이메일 인증 번호";
        log.info("전송해야될 메일: {}", email);

        if (redisClient.getData("ev", email) != null)
            redisClient.deleteData("ev",email);

        String authCode = createCode();
        log.info("생성된 인증번호: {}", authCode);
        String content = MailTemplate.signupCode(authCode);
        mailService.sendEmail(email, title, content);
        redisClient.setData("ev", email, authCode, authCodeExpirationMillis);
    }

    private String createCode(){
        int length = 6;
        try {
            SecureRandom sr = SecureRandom.getInstanceStrong();
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int index = sr.nextInt(charPool.length());
                code.append(charPool.charAt(index));
            }
            return code.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("인증번호 생성을 실패했습니다.");
        }
    }

    public void verifiedCode(String email, String code) {
        log.info("이메일 인증번호 확인");
        String upperCode = code.toUpperCase();
        if (redisClient.getData("ev", email) == null || !redisClient.getData("ev", email).equals(upperCode)) {
            throw new RuntimeException("인증번호가 일치하지 않습니다.");
        }
    }

    public void duplicateUsername(String username) {
        if (userRepository.existsByUsername(username))
            throw new DataIntegrityViolationException("이미 존재하는 아이디입니다.");
    }

    @Transactional
    public void signup(MultipartFile photo, AuthRequest authRequest) {
        String photoUrl = null;
        try {
            photoUrl = !photo.isEmpty() ? s3Client.uploadFile(photo) : null;
            User user = User.builder()
                    .photoUrl(photoUrl)
                    .email(authRequest.getEmail())
                    .username(authRequest.getUsername())
                    // todo password 해시화하기
                    .password(authRequest.getPassword())
                    .nickname(authRequest.getNickname())
                    .build();
            userRepository.save(user);
        } catch (DataIntegrityViolationException e ){
            // 회원가입 실패시 S3에 올라간 사진 삭제로 무결성 유지
            if (photoUrl != null)
                s3Client.delete(photoUrl);
            throw new DataIntegrityViolationException("이미 존재하는 값입니다.", e);
        }
    }
}
