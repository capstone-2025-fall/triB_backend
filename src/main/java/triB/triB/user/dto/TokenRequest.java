package triB.triB.user.dto;

import lombok.Getter;

@Getter
public class TokenRequest {
    private String deviceId;
    private String token;
}
