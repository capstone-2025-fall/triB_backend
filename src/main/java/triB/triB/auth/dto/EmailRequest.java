package triB.triB.auth.dto;

import lombok.Getter;

@Getter
public class EmailRequest {
    private String email;
    private String code;
}
