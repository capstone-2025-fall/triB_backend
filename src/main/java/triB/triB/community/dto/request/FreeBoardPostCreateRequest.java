package triB.triB.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FreeBoardPostCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 5000, message = "내용은 5000자 이내로 입력해주세요.")
    private String content;

    @NotEmpty(message = "최소 1개 이상의 해시태그를 선택해주세요.")
    @Size(max = 7, message = "해시태그는 최대 7개까지 선택 가능합니다.")
    private List<String> hashtags;  // Predefined 해시태그 이름 리스트

    // 이미지는 MultipartFile로 별도 처리
}
