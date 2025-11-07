package triB.triB.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 500, message = "댓글은 500자 이내로 입력해주세요.")
    private String content;

    // 대댓글인 경우에만 설정
    private Long parentCommentId;
}
