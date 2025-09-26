package triB.triB.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import triB.triB.auth.entity.IsAlarm;

@Getter
@Setter
@AllArgsConstructor
public class MyProfile {
    private String nickname;
    private String username;
    private String photoUrl;
    private IsAlarm isAlarm;
}
