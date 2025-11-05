package triB.triB.room.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.UserRepository;
import triB.triB.friendship.dto.UserResponse;
import triB.triB.friendship.repository.FriendRepository;
import triB.triB.room.dto.*;
import triB.triB.chat.entity.Message;
import triB.triB.room.entity.Room;
import triB.triB.room.entity.RoomStatus;
import triB.triB.room.entity.UserRoom;
import triB.triB.chat.repository.MessageRepository;
import triB.triB.room.entity.UserRoomId;
import triB.triB.room.repository.RoomReadStateRepository;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RoomReadStateRepository roomReadStateRepository;
    private final FriendRepository friendRepository;

    @Transactional(readOnly = true)
    public List<RoomsResponse> getRoomList(Long userId){
        List<UserRoom> userRooms = userRoomRepository.findAllWithRoomAndUsersByUser_UserId(userId);
        log.info("로그인한 유저의 room 목록 조회");
        return roomList(userRooms, userId);
    }

    @Transactional(readOnly = true)
    public List<RoomsResponse> searchRoomList(Long userId, String content){
        List<UserRoom> userRooms = userRoomRepository.findAllWithRoomAndUsersByUser_UserIdAndRoom_RoomName(userId, content);
        log.info("로그인한 유저가 검색하는 room 목록 조회");
        return roomList(userRooms, userId);
    }

//    // todo google api 나라 검색 찾기
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

    @Transactional
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

    @Transactional
    public void deleteRoom(Long userId, Long roomId) {
        UserRoom userRoom = userRoomRepository.findByUser_UserIdAndRoom_RoomId(userId, roomId);
        if (userRoom == null) {
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다");
        }
        userRoom.setRoomStatus(RoomStatus.EXIT);
    }

    @Transactional
    public void editChatRoom(Long userId, RoomEditRequest roomRequest) {
        Long roomId = roomRequest.getRoomId();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방이 존재하지 않습니다."));

        if (!userRoomRepository.existsByUser_UserIdAndRoom_RoomId(userId, roomId)){
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다");
        }

        String country = roomRequest.getCountry();
        String roomName = roomRequest.getRoomName();
        LocalDate startDate = roomRequest.getStartDate();
        LocalDate endDate = roomRequest.getEndDate();

        if (country != null) {
            room.setDestination(roomRequest.getCountry());
        }
        if (roomName != null) {
            room.setRoomName(roomRequest.getRoomName());
        }
        if (startDate != null) {
            room.setStartDate(startDate);
        }
        if (endDate != null) {
            room.setEndDate(endDate);
        }
    }

    @Transactional
    public void inviteFriends(Long userId, Long roomId, List<Long> userIds) {
        if (!userRoomRepository.existsByUser_UserIdAndRoom_RoomId(userId, roomId)){
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다");
        }
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방이 존재하지 않습니다."));

        for (Long id : userIds) {
            if (userRoomRepository.existsByUser_UserIdAndRoom_RoomId(id, roomId)){
                throw new DataIntegrityViolationException("이미 초대된 유저입니다.");
            }

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

            UserRoom ur = UserRoom.builder()
                    .user(user)
                    .room(room)
                    .build();
            userRoomRepository.save(ur);
        }
    }

    public List<UserResponse> getUsersInvitable(Long userId, Long roomId) {
        if (!userRoomRepository.existsByUser_UserIdAndRoom_RoomId(userId, roomId)){
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다");
        }

        return friendRepository.findAllFriendByUser(userId)
                .stream()
                .filter(user -> !userRoomRepository
                        .existsByUser_UserIdAndRoom_RoomId(user.getUserId(), roomId))
                .map(user -> UserResponse.builder()
                        .userId(user.getUserId())
                        .nickname(user.getNickname())
                        .photoUrl(user.getPhotoUrl())
                        .build())
                .toList();
    }

    // todo batch 조회 형식으로 수정해서 조회 최적화하기
    private List<RoomsResponse> roomList(List<UserRoom> userRooms, Long userId) {
        if (userRooms.isEmpty()) {
            log.info("유저가 참여한 채팅방이 없습니다.");
            return new ArrayList<>();
        }

        // 1. 모든 방 ID를 가져오기
        List<Room> rooms = userRooms.stream()
                .map(UserRoom::getRoom)
                .toList();

        List<Long> roomIds = rooms.stream()
                .map(Room::getRoomId)
                .toList();

        // 2. 모든 방의 사용자 정보를 단일 쿼리로 한번에 가져오기
        List<UserRoom> allUsersInRoom = userRoomRepository.findAllWithUsersByRoomIds(roomIds);

        Map<Long, List<String>> roomPhotoMap = allUsersInRoom.stream()
                .collect(Collectors.groupingBy(
                        ur -> ur.getRoom().getRoomId(),
                        LinkedHashMap::new,
                        Collectors.mapping(ur -> ur.getUser().getPhotoUrl(),
                                Collectors.toCollection(ArrayList::new))
                ));

        // 3. 모든 방의 마지막 메세지를 한꺼번에 가져오기
        List<Message> lastMessages = messageRepository.findLastMessagesByRooms(roomIds);
        Map<Long, Message> lastMessageMap = lastMessages.stream()
                .collect(Collectors.toMap(
                        msg -> msg.getRoom().getRoomId(),
                        msg -> msg
                ));

        // 4. 모든 방의 마지막으로 읽은 메세지 ID를 한꺼번에 가져와 Map으로 변환
        List<Object[]> lastMessageIds = roomReadStateRepository.findLastReadMessageIdsByRoomIdInAndUserId(roomIds, userId);
        Map<Long, Long> lastMessageIdMap = lastMessageIds.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        // 5. 읽지 않은 메세지 수를 배치로 가져오기
        Map<Long, Integer> notReadMessageTotalMap = new HashMap<>();

        List<Long> roomsWithReadState = lastMessageIdMap.keySet().stream().toList();
        List<Long> roomsWithoutReadState = roomIds.stream()
                .filter(id -> !lastMessageIdMap.containsKey(id))
                .toList();

        // 읽음 상태가 없는 방들의 전체 메세지 수 가져옴
        if (!roomsWithoutReadState.isEmpty()) {
            List<Object[]> totalCounts = messageRepository.countByRoomIdIn(roomsWithoutReadState);
            totalCounts.forEach(row -> notReadMessageTotalMap.put((Long) row[0], ((Number) row[1]).intValue()));
        }

        // todo N+1 쿼리 문제 발생
        // 읽음 상태가 있는 방들의 전체 메세지 수 가져옴
        if (!roomsWithReadState.isEmpty()) {
            for (Long roomId : roomsWithReadState) {
                Long lastReadMessageId = lastMessageIdMap.get(roomId);
                List<Object[]> unreadCounts = messageRepository.countByRoomIdAndMessageIdGreaterThan(roomId, lastReadMessageId);
                unreadCounts.forEach(row -> notReadMessageTotalMap.put(roomId, ((Number) row[1]).intValue()));
            }
        }

        // 6. 메모리에서 최종 응답을 구성함
        List<RoomsResponse> responses = new ArrayList<>();
        for (Room r : rooms) {
            Message msg = lastMessageMap.get(r.getRoomId());

            RoomsResponse response = RoomsResponse.builder()
                    .roomId(r.getRoomId())
                    .roomName(r.getRoomName())
                    .photoUrls(roomPhotoMap.getOrDefault(r.getRoomId(), new ArrayList<>()))
                    .destination(r.getDestination())
                    .startDate(r.getStartDate())
                    .endDate(r.getEndDate())
                    .content((msg != null) ? msg.getContent() : null)
                    .createdAt(r.getCreatedAt())
                    .messageNum(notReadMessageTotalMap.getOrDefault(r.getRoomId(), 0)) //todo 안 읽은 메세지 개수 세는 로직 필요 Entity 추가
                    .build();
            responses.add(response);
        }
        return responses;
    }
}
