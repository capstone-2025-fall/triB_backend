package triB.triB.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import triB.triB.auth.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
}
