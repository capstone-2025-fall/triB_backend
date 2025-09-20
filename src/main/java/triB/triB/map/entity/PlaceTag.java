package triB.triB.map.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceTag {
    LANDMARK("명소"),
    HOME("집"),
    RESTAURANT("음식점"),
    CAFE("카페"),
    MUSEUM("박물관"),
    PARK("공원"),
    SHOP("쇼핑"),
    ETC("기타");
    
    private final String description;
}
