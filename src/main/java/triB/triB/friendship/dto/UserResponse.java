package triB.triB.friendship.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String nickname;
    private String photoUrl;
}
