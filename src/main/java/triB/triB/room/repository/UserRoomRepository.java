package triB.triB.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triB.triB.auth.entity.User;
import triB.triB.room.entity.Room;
import triB.triB.room.entity.UserRoom;
import triB.triB.room.entity.UserRoomId;

import java.util.List;

@Repository
public interface UserRoomRepository extends JpaRepository<UserRoom, UserRoomId> {

    @Query("select distinct ur from UserRoom ur join fetch ur.user u join fetch ur.room r where u.userId = :userId " +
            "order by (select max(m.createdAt) from Message m where m.room = ur.room) desc")
    List<UserRoom> findAllWithRoomAndUsersByUser_UserId(@Param("userId") Long userId);

    @Query("select distinct ur from UserRoom ur join fetch ur.user u join fetch ur.room r where u.userId = :userId and lower(r.roomName) like lower(concat('%', :roomName, '%')) " +
            "order by (select max(m.createdAt) from Message m where m.room = ur.room) desc")
    List<UserRoom> findAllWithRoomAndUsersByUser_UserIdAndRoom_RoomName(@Param("userId") Long userId, @Param("roomName") String roomName);


    @Query("select distinct ur from UserRoom ur join fetch ur.user where ur.room.roomId in :roomIds order by ur.user.nickname asc")
    List<UserRoom> findAllWithUsersByRoomIds(@Param("roomIds") List<Long> roomIds);

    @Query("select ur.user from UserRoom ur where ur.room.roomId = :roomId order by ur.user.nickname asc")
    List<User> findUsersByRoomId(@Param("roomId") Long roomId);
}
