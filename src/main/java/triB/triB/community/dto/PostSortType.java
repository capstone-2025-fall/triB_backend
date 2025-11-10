package triB.triB.community.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostSortType {
    LATEST("최신순", "created_at", "DESC"),
    OLDEST("오래된순", "created_at", "ASC"),
    MOST_LIKED("좋아요 많은 순", "likes_count", "DESC"),
    MOST_COMMENTED("댓글 많은 순", "comments_count", "DESC");

    private final String description;
    private final String column;
    private final String direction;
}
