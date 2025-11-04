package triB.triB.chat.dto;

import lombok.Getter;

@Getter
public class PlaceRequest {
    private String placeId;
    private String displayName;
    private Double latitude;
    private Double longitude;
    private String photoUrl;
}
