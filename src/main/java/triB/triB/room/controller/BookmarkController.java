package triB.triB.room.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import triB.triB.global.response.ApiResponse;
import triB.triB.global.security.UserPrincipal;
import triB.triB.room.dto.BookmarkRequest;
import triB.triB.room.dto.BookmarkResponse;
import triB.triB.room.service.BookmarkService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookmarkResponse>> addBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody BookmarkRequest bookmarkRequest
    ){
        Long userId = userPrincipal.getUserId();
        BookmarkResponse response = bookmarkService.createBookmark(userId, bookmarkRequest.getRoomId(), bookmarkRequest.getContent());
        return ApiResponse.created("해당 북마크를 추가했습니다.", response);
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> updateBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody BookmarkRequest bookmarkRequest
    ){
        Long userId = userPrincipal.getUserId();
        bookmarkService.editBookmark(userId, bookmarkRequest.getBookmarkId(), bookmarkRequest.getContent());
        return ApiResponse.ok("해당 북마크를 수정했습니다.", null);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody BookmarkRequest bookmarkRequest
    ){
        Long userId = userPrincipal.getUserId();
        bookmarkService.removeBookmark(userId, bookmarkRequest.getBookmarkId());
        return ApiResponse.ok("해당 북마크를 삭제했습니다.", null);
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<BookmarkResponse>>> latestBookmarks(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "roomId") Long roomId
    ){
        Long userId = userPrincipal.getUserId();
        List<BookmarkResponse> response = bookmarkService.getLatestBookmarks(userId, roomId);
        return ApiResponse.ok("해당 북마크들을 조회했습니다.", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookmarkResponse>>> bookmarks(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "roomId") Long roomId
    ){
        Long userId = userPrincipal.getUserId();
        List<BookmarkResponse> response = bookmarkService.getBookmarks(userId, roomId);
        return ApiResponse.ok("해당 북마크들을 조회했습니다.", response);
    }
}
