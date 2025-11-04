package triB.triB.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlaceDetail {
    private String placeId;
    private String displayName;
    private Double latitude;
    private Double longitude;
    private String photoUrl;
}
