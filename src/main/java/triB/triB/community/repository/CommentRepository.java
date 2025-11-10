package triB.triB.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triB.triB.community.entity.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * 게시글의 모든 댓글 조회 (생성일 오름차순)
     */
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    /**
     * 부모 댓글의 대댓글 조회 (생성일 오름차순)
     */
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    /**
     * 게시글의 댓글 개수 조회
     */
    long countByPostId(Long postId);
}
