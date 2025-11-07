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
import triB.triB.community.dto.HashtagResponse;
import triB.triB.community.dto.PostSortType;
import triB.triB.community.dto.request.FreeBoardPostCreateRequest;
import triB.triB.community.dto.request.TripSharePostCreateRequest;
import triB.triB.community.dto.request.TripSharePostFilterRequest;
import triB.triB.community.dto.response.PostDetailsResponse;
import triB.triB.community.dto.response.PostSummaryResponse;
import triB.triB.community.entity.Hashtag;
import triB.triB.community.entity.TagType;
import triB.triB.community.repository.HashtagRepository;
import triB.triB.community.service.PostService;
import triB.triB.global.response.ApiResponse;
import triB.triB.global.security.UserPrincipal;

import java.util.stream.Collectors;

import java.util.List;

@Tag(name = "Community - Post", description = "커뮤니티 게시글 API")
@RestController
@RequestMapping("/api/v1/community/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final HashtagRepository hashtagRepository;

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

    @Operation(summary = "자유게시판 게시글 작성",
               description = "FREE_BOARD 타입 게시글을 작성합니다. Predefined 해시태그를 선택할 수 있습니다.")
    @PostMapping(value = "/free-board", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostDetailsResponse>> createFreeBoardPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestPart("request") FreeBoardPostCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        Long userId = userPrincipal.getUserId();
        PostDetailsResponse response = postService.createFreeBoardPost(userId, request, images);

        return ApiResponse.created("자유게시판 게시글이 작성되었습니다.", response);
    }

    @Operation(summary = "게시글 상세 조회",
               description = "게시글의 상세 정보를 조회합니다. (작성자, 이미지, 해시태그, 좋아요/댓글 수 포함)")
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailsResponse>> getPostDetails(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long currentUserId = userPrincipal != null ? userPrincipal.getUserId() : null;
        PostDetailsResponse response = postService.getPostDetails(postId, currentUserId);

        return ApiResponse.ok("게시글 상세 조회 성공", response);
    }

    @Operation(summary = "일정 공유 게시판 목록 조회",
               description = "나라, 정렬 조건, 검색어를 동시에 적용하여 TRIP_SHARE 게시글 목록을 조회합니다.")
    @GetMapping("/trip-share")
    public ResponseEntity<ApiResponse<List<PostSummaryResponse>>> getTripSharePosts(
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "LATEST") PostSortType sortType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        TripSharePostFilterRequest filter = TripSharePostFilterRequest.builder()
                .country(country)
                .sortType(sortType)
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();

        List<PostSummaryResponse> response = postService.getTripSharePosts(filter);
        return ApiResponse.ok("일정 공유 게시판 목록 조회 성공", response);
    }

    @Operation(summary = "Predefined 해시태그 목록 조회",
               description = "자유게시판에서 사용할 수 있는 Predefined 해시태그 목록을 조회합니다.")
    @GetMapping("/hashtags/predefined")
    public ResponseEntity<ApiResponse<List<HashtagResponse>>> getPredefinedHashtags() {
        List<Hashtag> hashtags = hashtagRepository.findByTagType(TagType.PREDEFINED);
        List<HashtagResponse> response = hashtags.stream()
                .map(HashtagResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.ok("Predefined 해시태그 목록 조회 성공", response);
    }
}
