package triB.triB.auth.dto;

import lombok.Getter;

@Getter
public class AuthRequest {
    private String email;
    private String username;
    private String password;
    private String nickname;
}
