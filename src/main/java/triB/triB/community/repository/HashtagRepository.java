package triB.triB.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triB.triB.community.entity.Hashtag;
import triB.triB.community.entity.TagType;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    /**
     * 해시태그 이름으로 조회
     */
    Optional<Hashtag> findByTagName(String tagName);

    /**
     * 해시태그 타입별 조회
     */
    List<Hashtag> findByTagType(TagType tagType);

    /**
     * 여러 해시태그 이름으로 조회 (IN 쿼리)
     */
    List<Hashtag> findByTagNameIn(List<String> tagNames);
}
