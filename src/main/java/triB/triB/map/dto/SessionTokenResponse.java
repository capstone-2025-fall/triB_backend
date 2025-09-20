package triB.triB.map.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionTokenResponse {
    private String sessionToken;
    private long expiresInSeconds;
}
