package triB.triB.chat.dto;

import lombok.Getter;
import triB.triB.chat.entity.PlaceTag;

@Getter
public class MessagePlaceRequest {
    private Long messageId;
    private PlaceTag placeTag;
}
