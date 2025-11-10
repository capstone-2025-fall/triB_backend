package triB.triB.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "날짜별 일정 그룹 응답")
public class DayScheduleResponse {

    @Schema(description = "여행 일차", example = "1")
    private Integer dayNumber;

    @Schema(description = "해당 날짜의 일정 목록")
    private List<ScheduleItemResponse> schedules;
}
