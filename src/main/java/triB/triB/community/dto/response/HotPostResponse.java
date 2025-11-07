package triB.triB.community.dto.response;

import lombok.Builder;
import lombok.Getter;
import triB.triB.community.entity.Post;

@Getter
@Builder
public class HotPostResponse {
    private Long postId;
    private String title;

    public static HotPostResponse from(Post post) {
        return HotPostResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .build();
    }
}
