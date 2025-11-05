//package triB.triB.global.infra;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import triB.triB.global.config.GoogleMapsConfig;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class GoogleMapsClient {
//
//    private final WebClient googleMapsWebClient;
//    private final GoogleMapsConfig googleMapsConfig;
//
//    public String autocompleteSearch(String input, String sessionToken) {
//        try {
//            log.debug("Google Places API (New) Autocomplete 요청");
//
//            // 요청 바디 구성
//            Map<String, Object> requestBody = new HashMap<>();
//            requestBody.put("input", input);
//            requestBody.put("sessionToken", sessionToken);
//
//            return googleMapsWebClient
//                    .post()
//                    .uri("/v1/places:autocomplete")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .header("X-Goog-FieldMask", "suggestions.placePrediction.text,suggestions.placePrediction.placeId,suggestions.placePrediction.structuredFormat")
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//        } catch (Exception ex) {
//            log.error("Google Places API (New) Autocomplete 호출 실패", ex);
//            throw new RuntimeException("Google Places API 호출 실패", ex);
//        }
//    }
//
//    public String placeDetails(String placeId, String sessionToken) {
//        try {
//            log.debug("Google Places API (New) Place Details 요청: placeId={}", placeId);
//
//            return googleMapsWebClient
//                    .get()
//                    .uri(uriBuilder -> uriBuilder
//                        .path("/v1/places/{placeId}")
//                        .queryParam("languageCode", "ko") // ⬅️ 여기에 언어 설정
//                        .build(placeId))
//                    .header("X-Goog-FieldMask", "id,displayName,location,primaryType,priceRange,regularOpeningHours,editorialSummary")
//                    .header("X-Goog-Session-Token", sessionToken)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//        } catch (Exception ex) {
//            log.error("Google Places API (New) Place Details 호출 실패", ex);
//            throw new RuntimeException("Google Places API 호출 실패", ex);
//        }
//    }
//
//    public String textSearch(String query) {
//        try {
//            log.debug("Google Places API (New) Text Search 요청");
//
//            // 요청 바디 구성
//            Map<String, Object> requestBody = new HashMap<>();
//            requestBody.put("textQuery", query);
//
//            return googleMapsWebClient
//                    .post()
//                    .uri("/v1/places:searchText")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .header("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.location,places.types,places.rating")
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//        } catch (Exception ex) {
//            log.error("Google Places API (New) Text Search 호출 실패", ex);
//            throw new RuntimeException("Google Places API 호출 실패", ex);
//        }
//    }
//
//    public String generateSessionToken() {
//        return java.util.UUID.randomUUID().toString();
//    }
//
//    public String autoCompleteSearchCountry(String input, String sessionToken) {
//        try {
//            log.debug("Google Place API (New) AutoComplete 국가 요청");
//
//            // 요청 바디 구성
//            Map<String, Object> requestBody = new HashMap<>();
//            requestBody.put("input", input);
//            requestBody.put("sessionToken", sessionToken);
//
//            return googleMapsWebClient
//                    .post()
//                    .uri("/v1/places:autocomplete")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .header("X-Goog-FieldMask", "")
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//        } catch (Exception ex) {
//            log.error("Google Places API (New) Autocomplete 호출 실패", ex);
//            throw new RuntimeException("Google Places API 호출 실패", ex);
//        }
//
//        }
//    }
//}
