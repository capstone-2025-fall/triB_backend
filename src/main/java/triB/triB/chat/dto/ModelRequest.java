package triB.triB.chat.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ModelPlaceRequest {
        private String placeName;
        private PlaceTag placeTag;
    }
}
