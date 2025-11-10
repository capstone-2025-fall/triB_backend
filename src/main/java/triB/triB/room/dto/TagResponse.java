package triB.triB.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import triB.triB.chat.entity.PlaceTag;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TagResponse {
    private Long tagId;
    private String placeName;
    private PlaceTag placeTag;
    private Double latitude;
    private Double longitude;
}
