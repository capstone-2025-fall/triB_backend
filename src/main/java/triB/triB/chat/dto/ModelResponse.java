package triB.triB.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import triB.triB.chat.entity.PlaceTag;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ModelResponse {
    private Integer budget;
    private List<Itinerary> itinerary;

    @Getter
    @Setter
    @Builder
    public static class Itinerary {
        private Integer day;
        private List<Visit> visits;
    }

    @Getter
    @Setter
    @Builder
    public static class Visit {
        private Integer order;
        private String displayName;
        private String nameAddress;
        private PlaceTag placeTag;
        private Double latitude;
        private Double longitude;
        private String arrival;
        private String departure;
        private Integer travelTime;
    }
}
