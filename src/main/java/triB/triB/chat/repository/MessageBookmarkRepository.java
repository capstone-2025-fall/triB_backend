package triB.triB.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import triB.triB.chat.entity.MessageBookmark;

import java.util.List;

@Repository
public interface MessageBookmarkRepository extends JpaRepository<MessageBookmark, Long> {

    MessageBookmark findByMessage_MessageId(Long messageId);

    @Query("select mb from MessageBookmark mb where mb.room.roomId=:roomId order by mb.bookmarkId desc")
    List<MessageBookmark> findByRoom_RoomId(Long roomId);

    @Query("select mb from MessageBookmark mb where mb.room.roomId=:roomId order by mb.bookmarkId desc limit 3")
    List<MessageBookmark> findByRoom_RoomIdLatest(Long roomId);
}
