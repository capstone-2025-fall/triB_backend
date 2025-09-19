//package triB.triB.auth.controller;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import triB.triB.auth.dto.OauthRequest;
//import triB.triB.auth.dto.RefreshTokenRequest;
//import triB.triB.global.response.ApiResponse;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/oauth")
//public class OauthController {
//
//    @PostMapping("/{provider}/token-exchange")
//    public ResponseEntity<ApiResponse<Map<String, String>>> oauth2Login(@PathVariable String provider, @RequestBody OauthRequest authRequest) {
//
//    }
//
//    @PostMapping("/{provider}/id-token")
//    public ResponseEntity<ApiResponse<Map<String, String>>> idToken(@PathVariable String provider, @RequestBody OauthRequest authRequest) {}
//
//    @PostMapping("/refresh")
//    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {}
//}
//
