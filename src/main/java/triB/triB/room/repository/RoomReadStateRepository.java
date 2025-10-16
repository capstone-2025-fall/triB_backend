package triB.triB.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triB.triB.room.entity.RoomReadState;
import triB.triB.room.entity.RoomReadStateId;

import java.util.List;

@Repository
public interface RoomReadStateRepository extends JpaRepository<RoomReadState, RoomReadStateId> {

    // 여러 방에 대한 마지막 읽은 메시지 ID를 한꺼번에 조회
    @Query("SELECT r.room.roomId, r.lastReadMessageId FROM RoomReadState r WHERE r.room.roomId IN :roomIds AND r.user.userId = :userId")
    List<Object[]> findLastReadMessageIdsByRoomIdInAndUserId(@Param("roomIds") List<Long> roomIds, @Param("userId") Long userId);
}
