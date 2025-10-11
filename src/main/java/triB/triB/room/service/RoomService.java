package triB.triB.room.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.UserRepository;
import triB.triB.room.dto.ChatUserResponse;
import triB.triB.room.dto.RoomRequest;
import triB.triB.room.dto.RoomResponse;
import triB.triB.room.dto.RoomsResponse;
import triB.triB.chat.entity.Message;
import triB.triB.room.entity.Room;
import triB.triB.room.entity.RoomStatus;
import triB.triB.room.entity.UserRoom;
import triB.triB.chat.repository.MessageRepository;
import triB.triB.room.entity.UserRoomId;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public List<RoomsResponse> getRoomList(Long userId){
        List<UserRoom> userRooms = userRoomRepository.findAllWithRoomAndUsersByUser_UserId(userId);
        log.info("로그인한 유저의 room 목록 조회");
        return roomList(userRooms);
    }

    public List<RoomsResponse> searchRoomList(Long userId, String content){
        List<UserRoom> userRooms = userRoomRepository.findAllWithRoomAndUsersByUser_UserIdAndRoom_RoomName(userId, content);
        log.info("로그인한 유저가 검색하는 room 목록 조회");
        return roomList(userRooms);
    }

    // todo google api 나라 검색 찾기
//    public List<String> getCountries(String country) {
//
//    }

    public List<ChatUserResponse> selectFriends(List<Long> userIds) {
        List<ChatUserResponse> responses = new ArrayList<>();
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
            log.info("nickname = {}, photoUrl = {}", user.getNickname(), user.getPhotoUrl());
            ChatUserResponse u = new ChatUserResponse(user.getNickname(), user.getPhotoUrl());
            responses.add(u);
        }
        return responses;
    }

    public RoomResponse makeChatRoom(Long userId, RoomRequest roomRequest) {

        Room room = Room.builder()
                .roomName(roomRequest.getRoomName())
                .destination(roomRequest.getCountry())
                .startDate(roomRequest.getStartDate())
                .endDate(roomRequest.getEndDate())
                .build();

        roomRepository.save(room);

        log.info("방 생성완료 roomId = {}", room.getRoomId());

        List<Long> userIds = roomRequest.getUserIds();
        userIds.add(userId);

        log.info("userIds = {}", userIds);

        for (Long id : userIds) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("유저가 존재하지 않습니다."));

            UserRoom userRoom = UserRoom.builder()
                    .id(new UserRoomId(user.getUserId(), room.getRoomId()))
                    .user(user)
                    .room(room)
                    .roomStatus(RoomStatus.ACTIVE)
                    .build();

            userRoomRepository.save(userRoom);
        }

        return new RoomResponse(room.getRoomId());
    }

    private List<RoomsResponse> roomList(List<UserRoom> userRooms){
        if (userRooms.isEmpty()) {
            log.info("유저가 참여한 채팅방이 없습니다.");
            return new ArrayList<>();
        }

        List<RoomsResponse> responses = new ArrayList<>();

        List<Room> rooms = userRooms.stream()
                .map(UserRoom::getRoom)
                .distinct().toList();


        for (Room r : rooms) {
            List<User> users = userRoomRepository.findUsersByRoomId(r.getRoomId());

            Message msg = messageRepository.findLastMessageByRoom(r);

            // todo 유저 프로필이 다 null 이면 리스트에 null 값만 남는 문제
            List<String> photoUrls = users.stream()
                    .map(User::getPhotoUrl)
                    .collect(Collectors.toCollection(ArrayList::new));

            log.info("photoUrls = {}", photoUrls);

            RoomsResponse response = RoomsResponse.builder()
                    .roomId(r.getRoomId())
                    .roomName(r.getRoomName())
                    .photoUrls(photoUrls)
                    .destination(r.getDestination())
                    .startDate(r.getStartDate())
                    .endDate(r.getEndDate())
                    .content((msg != null) ? msg.getContent() : null)
                    .createdAt(r.getCreatedAt())
                    .messageNum(0) //todo 안 읽은 메세지 개수 세는 로직 필요 Entity 추가
                    .build();
            responses.add(response);
        }
        return responses;
    }
}
