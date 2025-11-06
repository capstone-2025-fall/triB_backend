package triB.triB.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import triB.triB.community.dto.request.TripSharePostCreateRequest;
import triB.triB.community.dto.response.PostDetailsResponse;
import triB.triB.community.service.PostService;
import triB.triB.global.response.ApiResponse;
import triB.triB.global.security.UserPrincipal;

import java.util.List;

@Tag(name = "Community - Post", description = "커뮤니티 게시글 API")
@RestController
@RequestMapping("/api/v1/community/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "일정 공유 게시글 작성",
               description = "TRIP_SHARE 타입 게시글을 작성합니다. 여행 정보와 이미지를 함께 업로드할 수 있습니다.")
    @PostMapping(value = "/trip-share", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostDetailsResponse>> createTripSharePost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestPart("request") TripSharePostCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        Long userId = userPrincipal.getUserId();
        PostDetailsResponse response = postService.createTripSharePost(userId, request, images);

        return ApiResponse.created("일정 공유 게시글이 작성되었습니다.", response);
    }
}
