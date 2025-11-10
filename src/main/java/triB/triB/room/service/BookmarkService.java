package triB.triB.room.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triB.triB.chat.entity.MessageBookmark;
import triB.triB.chat.repository.MessageBookmarkRepository;
import triB.triB.room.dto.BookmarkResponse;
import triB.triB.room.entity.Room;
import triB.triB.room.entity.UserRoom;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final MessageBookmarkRepository messageBookmarkRepository;

    @Transactional
    public BookmarkResponse createBookmark(Long userId, Long roomId, String content){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방이 존재하지 않습니다."));

        if (userRoomRepository.findByUser_UserIdAndRoom_RoomId(userId, roomId) == null)
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");

        MessageBookmark mb = MessageBookmark.builder()
                .message(null)
                .content(content)
                .room(room)
                .build();
        messageBookmarkRepository.save(mb);
        return new BookmarkResponse(mb.getBookmarkId(), null);
    }

    @Transactional
    public void editBookmark(Long userId, Long bookmarkId, String content){
        MessageBookmark messageBookmark = messageBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new EntityNotFoundException("해당 북마크가 존재하지 않습니다."));

        if (userRoomRepository.findByUser_UserIdAndRoom_RoomId(userId, messageBookmark.getRoom().getRoomId()) == null)
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");

        messageBookmark.setContent(content);
        messageBookmarkRepository.save(messageBookmark);
    }

    @Transactional
    public void removeBookmark(Long userId, Long bookmarkId){
        MessageBookmark messageBookmark = messageBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new EntityNotFoundException("해당 북마크가 존재하지 않습니다."));

        if (userRoomRepository.findByUser_UserIdAndRoom_RoomId(userId, messageBookmark.getRoom().getRoomId()) == null)
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");

        messageBookmarkRepository.delete(messageBookmark);
    }

    public List<BookmarkResponse> getLatestBookmarks(Long userId, Long roomId){
        if (userRoomRepository.findByUser_UserIdAndRoom_RoomId(userId, roomId) == null)
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");

        return messageBookmarkRepository.findByRoom_RoomIdLatest(roomId).stream()
                .map(bookmark ->
                        new BookmarkResponse(bookmark.getBookmarkId(), bookmark.getContent()))
                .toList();
    }

    public List<BookmarkResponse> getBookmarks(Long userId, Long roomId){
        if (userRoomRepository.findByUser_UserIdAndRoom_RoomId(userId, roomId) == null)
            throw new BadCredentialsException("해당 채팅방에 대한 권한이 없습니다.");

        return messageBookmarkRepository.findByRoom_RoomId(roomId).stream()
                .map(bookmark ->
                        new BookmarkResponse(bookmark.getBookmarkId(), bookmark.getContent()))
                .toList();
    }

}
