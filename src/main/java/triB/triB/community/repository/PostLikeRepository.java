package triB.triB.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triB.triB.community.entity.PostLike;
import triB.triB.community.entity.PostLikeId;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    /**
     * 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
     */
    boolean existsByIdPostIdAndIdUserId(Long postId, Long userId);

    /**
     * 게시글의 좋아요 개수 조회
     */
    long countByIdPostId(Long postId);

    /**
     * 좋아요 취소 (삭제)
     */
    void deleteByIdPostIdAndIdUserId(Long postId, Long userId);
}
