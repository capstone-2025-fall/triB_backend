package triB.triB.friendship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triB.triB.friendship.entity.Friendship;
import triB.triB.friendship.entity.FriendshipStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    boolean existsByRequester_UserIdAndAddressee_UserId(Long userId, Long addresseeId);

    List<Friendship> findAllByAddressee_UserIdAndFriendshipStatusOrderByCreatedAtAsc(Long userId, FriendshipStatus status);

    Friendship findByRequester_UserIdAndAddressee_UserIdAndFriendshipStatus(Long userId, Long addresseeId, FriendshipStatus status);
}
