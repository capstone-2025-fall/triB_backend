package triB.triB.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import triB.triB.chat.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}
