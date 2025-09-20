package triB.triB.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GoogleMapsConfig {
    
    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;
    
    @Value("${google.maps.api.base-url:https://places.googleapis.com}")
    private String googleMapsBaseUrl;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    public String getApiKey() {
        return googleMapsApiKey;
    }
    
    public String getBaseUrl() {
        return googleMapsBaseUrl;
    }
}