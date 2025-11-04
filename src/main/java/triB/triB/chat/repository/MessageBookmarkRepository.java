package triB.triB.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triB.triB.chat.entity.MessageBookmark;

@Repository
public interface MessageBookmarkRepository extends JpaRepository<MessageBookmark, Long> {

    MessageBookmark findByMessage_MessageId(Long messageId);
}
