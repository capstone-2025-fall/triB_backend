package triB.triB.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import triB.triB.auth.entity.Token;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByUser_UserIdAndDeviceId(Long userId, String deviceId);
}