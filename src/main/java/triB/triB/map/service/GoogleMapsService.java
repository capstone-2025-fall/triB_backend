package triB.triB.map.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import triB.triB.global.infra.GoogleMapsClient;
import triB.triB.map.dto.AutocompleteResponse;
import triB.triB.map.dto.PlaceDetailsResponse;
import triB.triB.map.dto.SessionTokenResponse;
import triB.triB.map.dto.TextSearchResponse;


@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleMapsService {
    
    private final GoogleMapsClient googleMapsClient;
    private final ObjectMapper objectMapper;
    
    private static final long SESSION_TOKEN_EXPIRY_SECONDS = 180; // 3분
    
    public SessionTokenResponse generateSessionToken() {
        try {
            String sessionToken = googleMapsClient.generateSessionToken();
            log.info("세션 토큰 생성: {}", sessionToken);
            
            return new SessionTokenResponse(sessionToken, SESSION_TOKEN_EXPIRY_SECONDS);
        } catch (Exception e) {
            log.error("세션 토큰 생성 실패", e);
            throw new RuntimeException("세션 토큰 생성에 실패했습니다.", e);
        }
    }
    
    public AutocompleteResponse autocompleteSearch(String query, String sessionToken) {
        try {
            log.info("장소 자동완성 검색 요청: query={}, sessionToken={}", query, sessionToken);
            
            String response = googleMapsClient.autocompleteSearch(query, sessionToken);
            AutocompleteResponse autocompleteResponse = objectMapper.readValue(response, AutocompleteResponse.class);

            log.info("자동완성 검색 결과: {} 개", 
                    autocompleteResponse.getSuggestions() != null ? autocompleteResponse.getSuggestions().size() : 0);
            
            return autocompleteResponse;
        } catch (Exception e) {
            log.error("장소 자동완성 검색 실패: query={}, sessionToken={}", query, sessionToken, e);
            throw new RuntimeException("장소 자동완성 검색에 실패했습니다.", e);
        }
    }
    
    public PlaceDetailsResponse getPlaceDetails(String placeId, String sessionToken) {
        try {
            log.info("장소 상세 정보 요청: placeId={}, sessionToken={}", placeId, sessionToken);

            String response = googleMapsClient.placeDetails(placeId, sessionToken);
            log.info("Google API 원본 응답: {}", response);
            PlaceDetailsResponse placeDetailsResponse = objectMapper.readValue(response, PlaceDetailsResponse.class);

            log.info("장소 상세 정보 조회 완료: {}",
                    placeDetailsResponse.getDisplayName() != null ? placeDetailsResponse.getDisplayName().getText() : "null");

            return placeDetailsResponse;
        } catch (Exception e) {
            log.error("장소 상세 정보 조회 실패: placeId={}, sessionToken={}", placeId, sessionToken, e);
            throw new RuntimeException("장소 상세 정보 조회에 실패했습니다.", e);
        }
    }
    
    public TextSearchResponse textSearch(String query) {
        try {
            log.info("일반 장소 검색 요청: query={}", query);
            
            String response = googleMapsClient.textSearch(query);
            TextSearchResponse textSearchResponse = objectMapper.readValue(response, TextSearchResponse.class);
            
            log.info("일반 장소 검색 결과: {} 개", 
                    textSearchResponse.getPlaces() != null ? textSearchResponse.getPlaces().size() : 0);
            
            return textSearchResponse;
        } catch (Exception e) {
            log.error("일반 장소 검색 실패: query={}", query, e);
            throw new RuntimeException("일반 장소 검색에 실패했습니다.", e);
        }
    }
}
