package triB.triB.global.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import triB.triB.global.config.GoogleMapsConfig;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleMapsClient {
    
    private final RestTemplate restTemplate;
    private final GoogleMapsConfig googleMapsConfig;
    
    public String autocompleteSearch(String input, String sessionToken) {
        try {
            String url = googleMapsConfig.getBaseUrl() + "/v1/places:autocomplete";
            
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Goog-Api-Key", googleMapsConfig.getApiKey());
            headers.set("X-Goog-FieldMask", "suggestions.placePrediction.text,suggestions.placePrediction.placeId,suggestions.placePrediction.structuredFormat");
            
            // 요청 바디 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("input", input);
            requestBody.put("sessionToken", sessionToken);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.debug("Google Places API (New) Autocomplete 요청: {}", url);
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
        } catch (Exception ex) {
            log.error("Google Places API (New) Autocomplete 호출 실패", ex);
            throw new RuntimeException("Google Places API 호출 실패", ex);
        }
    }
    
    public String placeDetails(String placeId, String sessionToken) {
        try {
            String url = googleMapsConfig.getBaseUrl() + "/v1/places/" + placeId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Goog-Api-Key", googleMapsConfig.getApiKey());
            headers.set("X-Goog-FieldMask", "id,displayName,formattedAddress,location,types,rating,regularOpeningHours,internationalPhoneNumber,websiteUri");
            headers.set("X-Goog-Session-Token", sessionToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.debug("Google Places API (New) Place Details 요청: {}", url);
            // 5. 요청 방식을 GET으로 변경합니다.
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
            // --- 여기까지 ---
        } catch (Exception ex) {
            log.error("Google Places API (New) Place Details 호출 실패", ex);
            throw new RuntimeException("Google Places API 호출 실패", ex);
        }
    }
    
    public String textSearch(String query) {
        try {
            String url = googleMapsConfig.getBaseUrl() + "/v1/places:searchText";
            
            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Goog-Api-Key", googleMapsConfig.getApiKey());
            headers.set("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.location,places.types,places.rating");
            
            // 요청 바디 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("textQuery", query);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.debug("Google Places API (New) Text Search 요청: {}", url);
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
        } catch (Exception ex) {
            log.error("Google Places API (New) Text Search 호출 실패", ex);
            throw new RuntimeException("Google Places API 호출 실패", ex);
        }
    }
    
    public String generateSessionToken() {
        return java.util.UUID.randomUUID().toString();
    }
}
