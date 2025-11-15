package triB.triB.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import triB.triB.schedule.entity.Trip;
import triB.triB.schedule.entity.TripStatus;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    Trip findByRoomId(Long roomId);

    /**
     * 사용자가 참여한 특정 상태의 여행 목록 조회
     * @param userId 사용자 ID
     * @param tripStatus 여행 상태 (ACCEPTED, READY 등)
     * @return 여행 목록 (최신순)
     */
    @Query("SELECT DISTINCT t FROM Trip t " +
           "JOIN t.room r " +
           "WHERE t.tripStatus = :tripStatus " +
           "AND EXISTS (SELECT 1 FROM UserRoom ur WHERE ur.room.roomId = r.roomId AND ur.user.userId = :userId) " +
           "ORDER BY t.createdAt DESC")
    List<Trip> findByUserIdAndTripStatus(@Param("userId") Long userId, @Param("tripStatus") TripStatus tripStatus);
}
