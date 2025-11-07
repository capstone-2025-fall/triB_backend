package triB.triB.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triB.triB.chat.entity.Message;
import triB.triB.room.entity.Room;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 메세지 읽은 기록이 없는 room
    @Query("select m from Message m where m.room.roomId in :roomIds and m.messageId = (select max(m2.messageId) from Message m2 where m2.room.roomId = m.room.roomId)")
    List<Message> findLastMessagesByRooms(@Param("roomIds") List<Long> roomIds);

    // 메세지 읽은 기록이 있는 room
    @Query("select m.room.roomId, count(m) from Message m where m.room.roomId = :roomId and m.messageId > :lastReadMessageId group by m.room.roomId")
    List<Object[]> countByRoomIdAndMessageIdGreaterThan(@Param("roomId") Long roomId, @Param("lastReadMessageId") Long lastReadMessageId);

    @Query("select m.room.roomId, count(m) from Message m where m.room.roomId in :roomIds group by m.room.roomId")
    List<Object[]> countByRoomIdIn(@Param("roomIds") List<Long> roomIds);

    // 채팅방 나간 시점 가장 마지막 메세지
    @Query("select m.messageId from Message m where m.room.roomId = :roomId order by m.createdAt desc limit 1")
    Long findLastReadMessageIdByRoom_RoomId(Long roomId);

    List<Message> findAllByRoom_RoomIdOrderByCreatedAtAsc(Long roomId);
}