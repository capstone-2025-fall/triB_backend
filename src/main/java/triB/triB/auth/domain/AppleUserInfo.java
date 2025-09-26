package triB.triB.auth.domain;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class AppleUserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        return (String) attributes.get("provider");
    }

    @Override
    public String getProvider() {
        return "apple";
    }

    @Override
    public String getNickname() {
        return (String) attributes.get("nickname");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("profileImageUrl");
    }
}
