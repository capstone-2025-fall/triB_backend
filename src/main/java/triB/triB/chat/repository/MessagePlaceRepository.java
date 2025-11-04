package triB.triB.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triB.triB.chat.entity.MessagePlace;
import triB.triB.chat.entity.PlaceTag;

@Repository
public interface MessagePlaceRepository extends JpaRepository<MessagePlace, Long> {

    PlaceTag findPlaceTagByMessage_MessageId(@Param("messageId") Long messageId);

    MessagePlace findByMessage_MessageId(@Param("messageId") Long messageId);
}
