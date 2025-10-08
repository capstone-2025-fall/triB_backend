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

    private Location location;

    @JsonProperty("primaryType")
    private String primaryType;

    @JsonProperty("priceRange")
    private PriceRange priceRange;

    @JsonProperty("regularOpeningHours")
    private OpeningHours regularOpeningHours;

    @JsonProperty("editorialSummary")
    private EditorialSummary editorialSummary;

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

    @Getter
    @NoArgsConstructor
    public static class EditorialSummary {
        private String text;
        private String languageCode;
    }

    @Getter
    @NoArgsConstructor
    public static class PriceRange {
        @JsonProperty("startPrice")
        private Price startPrice;

        @JsonProperty("endPrice")
        private Price endPrice;
    }

    @Getter
    @NoArgsConstructor
    public static class Price {
        @JsonProperty("currencyCode")
        private String currencyCode;

        @JsonProperty("units")
        private String units;

        @JsonProperty("nanos")
        private Integer nanos;
    }
}
