package triB.triB.auth.dto;

import lombok.Getter;

@Getter
public class OauthRequest {
    private String authorizationCode;
    private String codeVerifier;
    private String redirectUrl;
    private String state;
}
