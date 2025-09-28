package triB.triB.friendship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triB.triB.friendship.entity.Friendship;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    boolean existsByRequester_UserIdAndAddressee_UserId(Long userId, Long addresseeId);
}
