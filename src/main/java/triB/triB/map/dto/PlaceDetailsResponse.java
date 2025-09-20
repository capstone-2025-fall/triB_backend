package triB.triB.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlaceDetailsResponse {
    
    private String id;
    
    @JsonProperty("displayName")
    private DisplayName displayName;
    
    @JsonProperty("formattedAddress")
    private String formattedAddress;
    
    private Location location;
    
    private List<String> types;
    
    private Double rating;
    
    @JsonProperty("regularOpeningHours")
    private OpeningHours regularOpeningHours;
    
    @JsonProperty("internationalPhoneNumber")
    private String phoneNumber;
    
    @JsonProperty("websiteUri")
    private String websiteUri;
    
    @Getter
    @NoArgsConstructor
    public static class DisplayName {
        private String text;
        private String languageCode;
    }
    
    @Getter
    @NoArgsConstructor
    public static class Location {
        private Double latitude;
        private Double longitude;
    }
    
    @Getter
    @NoArgsConstructor
    public static class OpeningHours {
        @JsonProperty("openNow")
        private Boolean openNow;
        
        @JsonProperty("weekdayDescriptions")
        private List<String> weekdayDescriptions;
    }
}
