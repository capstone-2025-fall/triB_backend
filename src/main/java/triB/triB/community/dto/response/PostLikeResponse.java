package triB.triB.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeResponse {

    private boolean isLiked;
    private Integer likesCount;

    public static PostLikeResponse of(boolean isLiked, Integer likesCount) {
        return PostLikeResponse.builder()
                .isLiked(isLiked)
                .likesCount(likesCount)
                .build();
    }
}
