package triB.triB.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import triB.triB.chat.entity.Message;
import triB.triB.room.entity.Room;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m where m.room = :room order by m.createdAt desc")
    Message findLastMessageByRoom(Room room);
}
