package triB.triB.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TextSearchResponse {
    
    private List<Place> places;
    
    @Getter
    @NoArgsConstructor
    public static class Place {
        private String id;
        
        @JsonProperty("displayName")
        private DisplayName displayName;
        
        @JsonProperty("formattedAddress")
        private String formattedAddress;
        
        private Location location;
        
        private List<String> types;
        
        private Double rating;
        
        @JsonProperty("priceLevel")
        private String priceLevel;
        
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
    }
}
