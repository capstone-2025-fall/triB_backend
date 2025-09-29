package triB.triB.friendship.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FriendRequest {
    private Long friendshipId;
    private Long userId;
    private String photoUrl;
    private String nickname;
}
