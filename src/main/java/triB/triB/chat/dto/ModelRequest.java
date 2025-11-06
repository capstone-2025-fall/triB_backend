package triB.triB.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import triB.triB.chat.entity.PlaceTag;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class ModelRequest {
    private Integer days;
    private LocalDate startDate;
    private String country;
    private Integer members;
    private List<ModelPlaceRequest> places;
    private List<String> mustVisit;
    private List<String> rule;
    private List<String> chat;

    @Getter
    @AllArgsConstructor
    public static class ModelPlaceRequest {
        private String placeName;
        private PlaceTag placeTag;
    }
}
