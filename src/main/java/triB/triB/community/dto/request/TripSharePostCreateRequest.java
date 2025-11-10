package triB.triB.community.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TripSharePostCreateRequest {

    @NotNull(message = "여행 ID는 필수입니다.")
    private Long tripId;

    // 제목은 해당 여행을 생성한 채팅방의 room_name을 사용
    // content는 일정 공유 게시글에서는 사용하지 않음
    // 이미지는 MultipartFile로 별도 처리
}
