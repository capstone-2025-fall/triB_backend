package triB.triB.room.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import triB.triB.global.response.ApiResponse;
import triB.triB.global.security.UserPrincipal;
import triB.triB.room.dto.TagResponse;
import triB.triB.room.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<TagResponse>>> latestTags(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "roomId") Long roomId
    ) {
        Long userId = userPrincipal.getUserId();
        List<TagResponse> response = tagService.getLatestTags(userId, roomId);
        return ApiResponse.ok("해당 장소들을 조회했습니다.", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> AllTags(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "roomId") Long roomId
    ) {
        Long userId = userPrincipal.getUserId();
        List<TagResponse> response = tagService.getAllTags(userId, roomId);
        return ApiResponse.ok("해당 장소들을 조회했습니다.", response);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteTags(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "tagId") Long tagId
    ){
        Long userId = userPrincipal.getUserId();
        tagService.deleteTag(userId, tagId);
        return ApiResponse.ok("해당 장소 태그를 삭제했습니다.", null);
    }
}
