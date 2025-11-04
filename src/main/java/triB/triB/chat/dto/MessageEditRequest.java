package triB.triB.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MessageEditRequest {
    private Long messageId;
    private String content;
}
