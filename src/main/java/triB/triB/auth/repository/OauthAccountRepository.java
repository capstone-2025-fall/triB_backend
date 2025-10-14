package triB.triB.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import triB.triB.auth.entity.OauthAccount;
import triB.triB.auth.entity.User;

import java.util.Optional;

public interface OauthAccountRepository extends JpaRepository<OauthAccount, Long> {

    Optional<OauthAccount> findByProviderAndProviderUserId(String provider, String providerId);

    OauthAccount findByUser(User user);

    OauthAccount findByUser_UserId(Long userId);

}