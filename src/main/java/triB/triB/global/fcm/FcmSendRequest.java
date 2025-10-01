package triB.triB.global.fcm;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmSendRequest {
    private RequestType requestType;
    private Long id; //nullable, null이 아닐땐 채팅방 조회!
    private String title;
    private String content;
    private String image; // 메세지인경우 프사!
    private String token;
}
