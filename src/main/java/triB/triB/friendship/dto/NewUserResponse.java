package triB.triB.friendship.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewUserResponse {
    private Long userId;
    private String nickname;
    private String photoUrl;
    private boolean isFriend;
}
