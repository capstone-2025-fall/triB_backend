package triB.triB.auth.security;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * OAuth2 토큰 요청을 커스터마이징하는 클래스
 * Apple 로그인의 경우 JWT 형식의 client_secret을 동적으로 생성하여 처리
 * 다른 OAuth2 제공자(Google, Kakao 등)는 일반 client_secret 사용
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomRequestEntityConverter implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    @Value("${spring.security.oauth2.client.registration.apple.client-id}")
    private String clientId;

    @Value("${apple.team.id}")
    private String teamId;

    @Value("${apple.login.key}")
    private String keyId;

    @Value("${apple.key.path}")
    private String keyPath;

    private final RestClient restClient = RestClient.builder()
            .messageConverters(converters -> {
                converters.add(0, new org.springframework.http.converter.FormHttpMessageConverter());
                converters.add(1, new org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter());
            })
            .build();

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {
        ClientRegistration clientRegistration = request.getClientRegistration();

        // Form 파라미터 구성
        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add("grant_type", request.getGrantType().getValue());
        formParameters.add("code", request.getAuthorizationExchange().getAuthorizationResponse().getCode());
        formParameters.add("redirect_uri", clientRegistration.getRedirectUri());
        formParameters.add("client_id", clientRegistration.getClientId());

        // Apple 로그인인 경우에만 client_secret JWT 생성
        if ("apple".equals(clientRegistration.getRegistrationId())) {
            try {
                String clientSecret = generateAppleClientSecret();
                formParameters.add("client_secret", clientSecret);
                log.info("Apple OAuth2 token request prepared with JWT client_secret");
            } catch (Exception e) {
                log.error("Failed to generate Apple client_secret", e);
                throw new OAuth2AuthorizationException(
                        new OAuth2Error("apple_jwt_error", "Failed to generate Apple client_secret: " + e.getMessage(), null),
                        e
                );
            }
        } else if (clientRegistration.getClientSecret() != null) {
            formParameters.add("client_secret", clientRegistration.getClientSecret());
        }

        // RestClient로 토큰 요청
        try {
            OAuth2AccessTokenResponse response = restClient.post()
                    .uri(clientRegistration.getProviderDetails().getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formParameters)
                    .retrieve()
                    .body(OAuth2AccessTokenResponse.class);

            log.info("Successfully retrieved OAuth2 token for registration: {}", clientRegistration.getRegistrationId());
            return response;
        } catch (Exception e) {
            log.error("Failed to retrieve OAuth2 token", e);
            throw new OAuth2AuthorizationException(
                    new OAuth2Error("token_request_error", "Failed to retrieve token: " + e.getMessage(), null),
                    e
            );
        }
    }

    private String generateAppleClientSecret() throws Exception {
        Instant now = Instant.now();
        Instant expiration = now.plus(180, ChronoUnit.DAYS); // 6개월

        return Jwts.builder()
                .header()
                    .add("kid", keyId)
                    .add("alg", "ES256")
                    .and()
                .issuer(teamId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(expiration))
                .audience().add("https://appleid.apple.com").and()
                .subject(clientId)
                .signWith(getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() throws Exception {

        try (FileInputStream resource = new FileInputStream(keyPath);
             Reader reader = new InputStreamReader(resource);
             PEMParser pemParser = new PEMParser(reader)) {

            Object pemObject = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            if (pemObject instanceof PrivateKeyInfo) {
                return converter.getPrivateKey((PrivateKeyInfo) pemObject);
            }

            throw new IllegalArgumentException("Invalid private key format");
        }
    }
}
