package triB.triB.community.dto.response;

import lombok.Builder;
import lombok.Getter;
import triB.triB.community.entity.Post;

import java.time.LocalDateTime;

@Getter
@Builder
public class HotPostResponse {
    private Long postId;
    private String title;
    private Integer likesCount;
    private Integer commentsCount;
    private LocalDateTime createdAt;

    public static HotPostResponse from(Post post) {
        return HotPostResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
