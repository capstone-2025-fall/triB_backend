package triB.triB.map.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceSource {
    MAP("지도 선택"),
    TAG("메시지 태깅");
    
    private final String description;
}
