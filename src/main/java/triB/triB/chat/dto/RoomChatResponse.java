package triB.triB.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class RoomChatResponse {
    private String roomName;
    private List<MessageResponse> messages;
}
