package triB.triB.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GeminiResponse {
    private String summarize;
    private String travelMode;
    private String itinerary;
    private Details details;

    @Getter
    @Setter
    public static class Details {
        private List<String> airports;
        private List<String> accommodations;
        private Map<String, Schedule> schedule;
    }

    @Getter
    @Setter
    public static class Schedule {
        private String start;
        private String end;
    }
}
