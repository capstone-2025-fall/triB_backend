package triB.triB.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triB.triB.chat.entity.MessagePlaceDetail;

@Repository
public interface MessagePlaceDetailRepository extends JpaRepository<MessagePlaceDetail, Long> {
    MessagePlaceDetail findByMessage_MessageId (Long messageId);
}
