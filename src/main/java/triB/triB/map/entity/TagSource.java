package triB.triB.map.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagSource {
    AUTO("자동"),
    USER("사용자");
    
    private final String description;
}
