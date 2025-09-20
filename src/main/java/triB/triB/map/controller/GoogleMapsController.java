package triB.triB.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import triB.triB.global.response.ApiResponse;
import triB.triB.map.dto.AutocompleteResponse;
import triB.triB.map.dto.PlaceDetailsResponse;
import triB.triB.map.dto.SessionTokenResponse;
import triB.triB.map.dto.TextSearchResponse;
import triB.triB.map.service.GoogleMapsService;

@Tag(name = "Google Maps API", description = "Google Maps 장소 검색 API")
@RestController
@RequestMapping("/api/v1/messages/map")
@RequiredArgsConstructor
public class GoogleMapsController {
    
    private final GoogleMapsService googleMapsService;
    
    @Operation(summary = "세션 토큰 생성", description = "장소 자동완성을 위한 세션 토큰을 생성합니다 (3분 유효)")
    @GetMapping("/session")
    public ResponseEntity<ApiResponse<SessionTokenResponse>> generateSessionToken() {
        SessionTokenResponse response = googleMapsService.generateSessionToken();
        return ApiResponse.ok("세션 토큰이 생성되었습니다.", response);
    }
    
    @Operation(summary = "장소 자동완성 검색", description = "세션 토큰을 사용하여 장소 자동완성 검색을 수행합니다 (무료)")
    @GetMapping
    public ResponseEntity<ApiResponse<AutocompleteResponse>> autocompleteSearch(
            @Parameter(description = "검색어", required = true, example = "강남역")
            @RequestParam("query") String query,
            @Parameter(description = "세션 토큰", required = true)
            @RequestParam("sessionToken") String sessionToken) {
        
        AutocompleteResponse response = googleMapsService.autocompleteSearch(query, sessionToken);
        return ApiResponse.ok("장소 자동완성 검색이 완료되었습니다.", response);
    }
    
    @Operation(summary = "장소 상세 정보 조회", description = "세션 토큰을 사용하여 장소 상세 정보를 조회합니다 (유료, 세션 완료)")
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<PlaceDetailsResponse>> getPlaceDetails(
            @Parameter(description = "Google Place ID", required = true)
            @RequestParam("placeId") String placeId,
            @Parameter(description = "세션 토큰", required = true)
            @RequestParam("sessionToken") String sessionToken) {
        
        PlaceDetailsResponse response = googleMapsService.getPlaceDetails(placeId, sessionToken);
        return ApiResponse.ok("장소 상세 정보 조회가 완료되었습니다.", response);
    }
    
    @Operation(summary = "일반 장소 검색", description = "Text Search API를 사용한 일반 장소 검색입니다")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<TextSearchResponse>> textSearch(
            @Parameter(description = "검색어", required = true, example = "서울 맛집")
            @RequestParam("query") String query) {
        
        TextSearchResponse response = googleMapsService.textSearch(query);
        return ApiResponse.ok("일반 장소 검색이 완료되었습니다.", response);
    }
}
