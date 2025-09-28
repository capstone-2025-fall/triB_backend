package triB.triB.friendship.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.UserRepository;
import triB.triB.friendship.dto.NewUserResponse;
import triB.triB.friendship.dto.UserResponse;
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

    public Map<String, Object> getMyUsername(Long userId){
        Map<String, Object> map = new HashMap<>();
        map.put("username", userRepository.findUsernameById(userId));
        return map;
    }

    public NewUserResponse searchNewFriend(Long userId, String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 아이디의 유저가 존재하지 않습니다."));

        // requester가 상대고, addressee가 나인 경우 = 이미 유저가 나에게 친구추가를 보낸 경우
        if (friendshipRepository.existsByRequester_UserIdAndAddressee_UserId(user.getUserId(), userId))
            throw new DataIntegrityViolationException("상대가 이미 보낸 친구 요청이 대기 중입니다. 수신함에서 확인하세요.");

        boolean isFriend = friendRepository.existsByUser_UserIdAndFriend_UserId(userId, user.getUserId());

        return new NewUserResponse(
                user.getUserId(),
                user.getNickname(),
                user.getPhotoUrl(),
                isFriend
        );
    }

}
