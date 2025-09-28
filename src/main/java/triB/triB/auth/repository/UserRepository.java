package triB.triB.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triB.triB.auth.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("select u.username from User u where u.userId = :userId")
    String findUsernameById(@Param("userId")Long userId);

    Optional<User> findByUsername(String username);
}