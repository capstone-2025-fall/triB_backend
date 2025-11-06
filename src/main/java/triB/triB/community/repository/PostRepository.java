package triB.triB.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triB.triB.community.entity.Post;
import triB.triB.community.entity.PostType;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    // 기본 CRUD는 JpaRepository가 제공

    /**
     * 게시글 ID와 사용자 ID로 게시글 조회
     */
    Optional<Post> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * 게시글 타입별 목록 조회 (생성일 내림차순)
     */
    List<Post> findByPostTypeOrderByCreatedAtDesc(PostType postType);

    /**
     * 게시글 타입별 개수 조회
     */
    long countByPostType(PostType postType);
}
