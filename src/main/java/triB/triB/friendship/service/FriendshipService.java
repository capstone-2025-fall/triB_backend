package triB.triB.friendship.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.UserRepository;
import triB.triB.friendship.dto.FriendRequest;
import triB.triB.friendship.dto.NewUserResponse;
import triB.triB.friendship.dto.UserResponse;
import triB.triB.friendship.entity.Friend;
import triB.triB.friendship.entity.Friendship;
import triB.triB.friendship.entity.FriendshipStatus;
import triB.triB.friendship.repository.FriendRepository;
import triB.triB.friendship.repository.FriendshipRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public List<UserResponse> getMyFriends(Long userId){

        List<User> friends = friendRepository.findAllFriendByUser(userId);
        List<UserResponse> result = new ArrayList<>();

        friends.forEach(friend -> {
            UserResponse u = new UserResponse(friend.getUserId(), friend.getNickname(), friend.getPhotoUrl());
            result.add(u);
        });
        return result;
    }

    public UserResponse getMyProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저가 존재하지 않습니다."));

        return new UserResponse(
                user.getUserId(),
                user.getNickname(),
                user.getPhotoUrl());
    }

    public List<UserResponse> searchMyFriends(Long userId, String nickname){
        List<User> friends = friendRepository.findAllFriendByUserAndFriend_Nickname(userId, nickname);
        List<UserResponse> result = new ArrayList<>();

        friends.forEach(friend -> {
            UserResponse u = new UserResponse(friend.getUserId(), friend.getNickname(), friend.getPhotoUrl());
            result.add(u);
        });
        return result;
    }

    public NewUserResponse searchNewFriend(Long userId, String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 아이디의 유저가 존재하지 않습니다."));

        boolean isFriend = friendRepository.existsByUser_UserIdAndFriend_UserId(userId, user.getUserId());

        return new NewUserResponse(
                user.getUserId(),
                user.getNickname(),
                user.getPhotoUrl(),
                isFriend
        );
    }

    // 친구 요청 보내기
    @Transactional
    public void requestFriendshipToUser(Long userId1, Long userId2){
        User requester = userRepository.findById(userId1)
                .orElseThrow(() -> new EntityNotFoundException("친구 요청을 보내는 유저가 존재하지 않습니다."));

        User addressee = userRepository.findById(userId2)
                .orElseThrow(() -> new EntityNotFoundException("친구 요청을 보낼 유저가 존재하지 않습니다."));

        if (friendshipRepository.existsByRequester_UserIdAndAddressee_UserIdAndFriendshipStatus(userId1, userId2, FriendshipStatus.REJECTED))
            throw new RuntimeException("이미 친구 수락 거부당한 유저입니다.");

        if (friendshipRepository.existsByRequester_UserIdAndAddressee_UserId(userId1, userId2)) {
            throw new DataIntegrityViolationException("FRIEND_REQUEST_ALREADY_SENT: 이미 친구요청을 보낸 유저입니다.");
        }

        // requester가 상대고, addressee가 나인 경우 = 이미 유저가 나에게 친구추가를 보낸 경우
        if (friendshipRepository.existsByRequester_UserIdAndAddressee_UserId(userId2, userId1))
            throw new DataIntegrityViolationException("INBOUND_REQUEST_PENDING: 상대가 이미 보낸 친구 요청이 대기 중입니다. 수신함에서 확인하세요.");

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .friendshipStatus(FriendshipStatus.PENDING)
                .build();

        friendshipRepository.save(friendship);

        // todo FCM 메세지 알림 보내기 userId2에게
    }

    // 내게 온 요청 확인
    public List<FriendRequest> getMyRequests(Long userId){
        List<Friendship> requests = friendshipRepository.findAllByAddressee_UserIdAndFriendshipStatusOrderByCreatedAtAsc(userId, FriendshipStatus.PENDING);
        List<FriendRequest> result = new ArrayList<>();

        requests.forEach(friendship -> {
            User requester = friendship.getRequester();
            FriendRequest f = new FriendRequest(friendship.getFriendshipId(), requester.getUserId(), requester.getPhotoUrl(), requester.getNickname());
            result.add(f);
        });
        return result;
    }

    // 내게 온 친구요청 수락
    @Transactional
    public void acceptMyFriendship(Long userId, Long friendshipId){
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("해당 요청이 존재하지 않습니다."));

        if (!userId.equals(friendship.getAddressee().getUserId()))
            throw new BadCredentialsException("해당 요청을 수락할 수 없습니다.");

        friendship.setFriendshipStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        Friend friend1 = Friend.builder()
                .user(friendship.getRequester())
                .friend(friendship.getAddressee())
                .build();

        Friend friend2 = Friend.builder()
                .user(friendship.getAddressee())
                .friend(friendship.getRequester())
                .build();

        friendRepository.save(friend1);
        friendRepository.save(friend2);

        // todo FCM 메세지 알림 보내기 friendship.getRequester
    }

    // 내게 온 친구요청 거절
    @Transactional
    public void rejectMyFriendship(Long userId, Long friendshipId){
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("해당 요청이 존재하지 않습니다."));

        if (!userId.equals(friendship.getAddressee().getUserId()))
            throw new BadCredentialsException("해당 요청을 거절할 수 없습니다.");

        friendship.setFriendshipStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);

        // todo FCM 메세지 알림 보내기 friendship.getRequester에게(이건안보내도될듯)
    }
}
