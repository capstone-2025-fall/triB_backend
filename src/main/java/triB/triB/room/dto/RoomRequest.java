package triB.triB.room.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class RoomRequest {
    private String country;
    private String roomName;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> userIds;
}
