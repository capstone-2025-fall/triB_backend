package triB.triB.community.dto.response;

import lombok.Builder;
import lombok.Getter;
import triB.triB.auth.entity.User;
import triB.triB.community.dto.AuthorResponse;
import triB.triB.community.entity.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class CommentResponse {

    private Long commentId;
    private Long postId;
    private Long parentCommentId;
    private AuthorResponse author;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies;  // 대댓글 목록

    public static CommentResponse from(Comment comment, User author, List<CommentResponse> replies) {
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPostId())
                .parentCommentId(comment.getParentCommentId())
                .author(AuthorResponse.from(author))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replies(replies != null ? replies : new ArrayList<>())
                .build();
    }
}
