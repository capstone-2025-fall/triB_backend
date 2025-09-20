package triB.triB.map.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triB.triB.map.entity.MessagePlace;
import triB.triB.map.entity.PlaceTag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessagePlaceRepository extends JpaRepository<MessagePlace, Long> {
    
    Optional<MessagePlace> findByMessageId(Long messageId);
    
    List<MessagePlace> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    
    List<MessagePlace> findByRoomIdAndPlaceTagOrderByCreatedAtDesc(Long roomId, PlaceTag placeTag);
    
    @Query("SELECT mp FROM MessagePlace mp WHERE mp.roomId = :roomId AND mp.createdAt BETWEEN :startDate AND :endDate ORDER BY mp.createdAt DESC")
    List<MessagePlace> findByRoomIdAndCreatedAtBetween(@Param("roomId") Long roomId, 
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT mp FROM MessagePlace mp WHERE mp.roomId = :roomId AND mp.placeTag = :placeTag AND mp.createdAt BETWEEN :startDate AND :endDate ORDER BY mp.createdAt DESC")
    List<MessagePlace> findByRoomIdAndPlaceTagAndCreatedAtBetween(@Param("roomId") Long roomId, 
                                                                  @Param("placeTag") PlaceTag placeTag,
                                                                  @Param("startDate") LocalDateTime startDate, 
                                                                  @Param("endDate") LocalDateTime endDate);
    
    long countByRoomId(Long roomId);
    
    long countByRoomIdAndPlaceTag(Long roomId, PlaceTag placeTag);
}
