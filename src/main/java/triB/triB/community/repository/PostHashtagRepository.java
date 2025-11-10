package triB.triB.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triB.triB.community.entity.PostHashtag;
import triB.triB.community.entity.PostHashtagId;

import java.util.List;

@Repository
public interface PostHashtagRepository extends JpaRepository<PostHashtag, PostHashtagId> {
    /**
     * 게시글의 모든 해시태그 조회
     */
    List<PostHashtag> findByIdPostId(Long postId);

    /**
     * 게시글의 모든 해시태그 삭제
     */
    void deleteByIdPostId(Long postId);
}
