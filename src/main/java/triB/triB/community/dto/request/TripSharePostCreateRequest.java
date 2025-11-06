package triB.triB.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TripSharePostCreateRequest {

    @NotNull(message = "여행 ID는 필수입니다.")
    private Long tripId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 5000, message = "내용은 5000자 이내로 입력해주세요.")
    private String content;

    // 이미지는 MultipartFile로 별도 처리
}
