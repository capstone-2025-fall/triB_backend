package triB.triB.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AutocompleteResponse {
    
    private List<Suggestion> suggestions;
    
    @Getter
    @NoArgsConstructor
    public static class Suggestion {
        @JsonProperty("placePrediction")
        private PlacePrediction placePrediction;
        
        @Getter
        @NoArgsConstructor
        public static class PlacePrediction {
            private String placeId;
            private TextContent text;
            
            @JsonProperty("structuredFormat")
            private StructuredFormat structuredFormat;
            
            @Getter
            @NoArgsConstructor
            public static class TextContent {
                private String text;
                private List<TextMatch> matches;
                
                @Getter
                @NoArgsConstructor
                public static class TextMatch {
                    @JsonProperty("endOffset")
                    private Integer endOffset;
                }
            }
            
            @Getter
            @NoArgsConstructor
            public static class StructuredFormat {
                @JsonProperty("mainText")
                private TextContent mainText;
                
                @JsonProperty("secondaryText")
                private TextContent secondaryText;
            }
        }
    }
}
