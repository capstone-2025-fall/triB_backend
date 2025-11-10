package triB.triB.community.dto;

import lombok.Builder;
import lombok.Getter;
import triB.triB.auth.entity.User;

@Getter
@Builder
public class AuthorResponse {
    private Long userId;
    private String nickname;
    private String photoUrl;

    public static AuthorResponse from(User user) {
        return AuthorResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .photoUrl(user.getPhotoUrl())
                .build();
    }
}
