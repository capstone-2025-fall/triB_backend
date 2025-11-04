package triB.triB.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import triB.triB.friendship.dto.UserResponse;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MessageResponse {
    private ActionType actionType;
    private UserResponse user;
    private MessageDto message;
    private LocalDateTime createdAt;
}
