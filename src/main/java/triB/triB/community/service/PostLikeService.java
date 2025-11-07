package triB.triB.community.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.UserRepository;
import triB.triB.community.entity.Post;
import triB.triB.community.entity.PostLike;
import triB.triB.community.entity.PostLikeId;
import triB.triB.community.exception.PostNotFoundException;
import triB.triB.community.repository.PostLikeRepository;
import triB.triB.community.repository.PostRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void toggleLike(Long postId, Long userId) {
        // 1. Post 존재 확인
        Post post = postRepository.findById(postId)
            .orElseThrow(PostNotFoundException::new);

        // 2. User 존재 확인
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 3. 이미 좋아요를 눌렀는지 확인
        boolean exists = postLikeRepository.existsByIdPostIdAndIdUserId(postId, userId);

        if (exists) {
            // 좋아요 취소
            postLikeRepository.deleteByIdPostIdAndIdUserId(postId, userId);
            post.decrementLikesCount();
        } else {
            // 좋아요 추가
            PostLikeId likeId = new PostLikeId(postId, userId);

            PostLike postLike = PostLike.builder()
                .id(likeId)
                .post(post)
                .user(user)
                .build();

            postLikeRepository.save(postLike);
            post.incrementLikesCount();
        }

        postRepository.save(post);
    }

    public boolean isLikedByUser(Long postId, Long userId) {
        return postLikeRepository.existsByIdPostIdAndIdUserId(postId, userId);
    }
}
