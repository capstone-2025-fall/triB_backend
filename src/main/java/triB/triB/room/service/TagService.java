package triB.triB.room.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import triB.triB.chat.entity.MessagePlace;
import triB.triB.chat.entity.MessagePlaceDetail;
import triB.triB.chat.repository.MessagePlaceDetailRepository;
import triB.triB.chat.repository.MessagePlaceRepository;
import triB.triB.room.dto.TagResponse;
import triB.triB.room.repository.UserRoomRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final UserRoomRepository userRoomRepository;
    private final MessagePlaceRepository messagePlaceRepository;
    private final MessagePlaceDetailRepository messagePlaceDetailRepository;

    public List<TagResponse> getLatestTags(Long userId, Long roomId){
        if (userRoomRepository.findByUser_UserIdAndRoom_RoomId(userId, roomId) == null)
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");

        return messagePlaceToTagResponse(messagePlaceRepository.findByRoom_RoomIdLatest(roomId));
    }

    public List<TagResponse> getAllTags(Long userId, Long roomId){
        if (userRoomRepository.findByUser_UserIdAndRoom_RoomId(userId, roomId) == null)
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");

        return messagePlaceToTagResponse(messagePlaceRepository.findByRoom_RoomId(roomId));
    }

    public void deleteTag(Long userId, Long tagId){
        MessagePlace mp = messagePlaceRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("해당 태그가 존재하지 않습니다."));

        if (userRoomRepository.findByUser_UserIdAndRoom_RoomId(userId, mp.getRoom().getRoomId()) == null)
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");

        messagePlaceRepository.deleteById(tagId);
    }

    private List<TagResponse> messagePlaceToTagResponse(List<MessagePlace> messagePlaces){
        return messagePlaces.stream()
                .map(messagePlace -> {
                    MessagePlaceDetail m = messagePlaceDetailRepository.findByMessage_MessageId(messagePlace.getMessage().getMessageId());
                    if (m == null)
                        throw new EntityNotFoundException("해당 장소 정보가 존재하지 않습니다.");
                    return TagResponse.builder()
                            .tagId(messagePlace.getMessagePlaceId())
                            .placeName(m.getDisplayName())
                            .placeTag(messagePlace.getPlaceTag())
                            .latitude(m.getLatitude())
                            .longitude(m.getLongitude())
                            .build();
                })
                .toList();
    }
}
