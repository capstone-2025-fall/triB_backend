//package triB.triB.global.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Configuration
//public class GoogleMapsConfig {
//
//    @Value("${google.maps.api.key}")
//    private String googleMapsApiKey;
//
//    @Value("${google.maps.api.base-url:https://places.googleapis.com}")
//    private String googleMapsBaseUrl;
//
//    @Bean
//    public WebClient googleMapsWebClient() {
//        return WebClient.builder()
//                .baseUrl(googleMapsBaseUrl)
//                .defaultHeader("X-Goog-Api-Key", googleMapsApiKey)
//                .build();
//    }
//
//    public String getApiKey() {
//        return googleMapsApiKey;
//    }
//
//    public String getBaseUrl() {
//        return googleMapsBaseUrl;
//    }
//}