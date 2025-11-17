package triB.triB.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triB.triB.community.entity.Post;
import triB.triB.community.entity.PostLike;
import triB.triB.community.entity.PostLikeId;
import triB.triB.community.entity.PostType;

import java.util.List;

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

    @Query("select p from PostLike pl join pl.post p left join fetch p.trip t left join fetch t.room join fetch p.user " +
            "where pl.user.userId = :userId and p.postType = :postType order by pl.createdAt desc")
    List<Post> findPostByUser_UserIdAndPostTypeOrderByIdDesc(@Param("userId") Long userId, @Param("postType") PostType postType);
}