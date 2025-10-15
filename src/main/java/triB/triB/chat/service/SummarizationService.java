package triB.triB.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import triB.triB.chat.dto.GeminiRequest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummarizationService {

    @Value("${gemini.base-url}")
    private String baseUrl;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.api-key}")
    private String apiKey;

    private final @Qualifier("geminiWebClient") WebClient geminiWebClient;

    // todo Mono 추가하기
    public Mono<String> summarizeChat(List<String> messages) {

        String prompt = """
                
                당신은 긴 채팅 로그에서 여행 선호도를 분석하고, 이를 바탕으로 여행 유형을 한 문장으로 요약하는 시스템입니다.
                
                [규칙]
                1) 의미 없는 토큰과 잡음(예: ㅋㅋ, ㅎㅎ, 이모티콘, 반복 자음·모음, 단순 반응 “ㅇㅋ”, “네”)은 분석에서 제외.
                2) 같은 의미의 메시지는 군집화하여 중복 의도를 통합하여 제거.
                3) 대화속 **핵심 의도**를 **가성비, 미식, 자유여행, 여유로운 페이스**와 같은 키워드로 정의하고, 이를 기반으로 여행 유형을 요약.
                4) 장소, 음식, 활동 등 구체적인 선호/기피 요소를 명확히 언급하여 요약에 포함
                5) 안전, 예산, 이동 방식 등 부가적인 정보 요약에 추가
                6) 장소·업체·서비스명이 반복 등장하면 별도 [places] 목록에 추출(고유명/유형/등장횟수).
                7) 한국어 평서문으로만 출력. 따옴표나 부가설명 금지
                8) 정보가 부족하면 “정보 부족으로 선호 파악이 어려움(추정: …)” 형태로 한 문장으로 답변.
                9) 채팅 내용을 분석해서 travelMode가 DRIVE, TRANSIT 두가지 중 하나로 결정해서 답해줘
                
                출력값 형태)
                
                한국어 문장으로 어떤 여행을 하고싶다 라는 문장으로 끝내야돼.
                채팅 내용을 DRIVE, TRANSIT
                
                """;

        String msg = IntStream.range(0, messages.size())
                .mapToObj(i -> (i + 1) + ". " + messages.get(i))
                .collect(Collectors.joining("\n"));

        // Gemini 에게 보낼 request 만듦
        GeminiRequest request = GeminiRequest.builder()
                .systemInstruction(
                        GeminiRequest.SystemInstruction.builder()
                                .parts(List.of(
                                        GeminiRequest.ofText(prompt)
                                ))
                                .build()
                )
                .contents(List.of(
                        GeminiRequest.Content.builder()
                                .parts(List.of(
                                        GeminiRequest.ofText(msg)
                                ))
                                .build()
                ))
                .build();

        // Gemini와 통신해야할 주소
        String url = "/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        // Gemini와 통신하는 과정
        Mono<String> response = geminiWebClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        res -> res.createException().flatMap(Mono::error)
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        res -> res.createException().flatMap(Mono::error)
                )
                .bodyToMono(String.class)
                .map(body -> {
                    try {
                        JsonNode root = new ObjectMapper().readTree(body);
                        return root.at("/candidates/0/content/parts/0/text").asText();
                    } catch (Exception e) {
                        throw new RuntimeException("Gemini 응답 파싱에 실패했습니다.");
                    }
                });

        log.info(String.valueOf(response));
        return response;
    }


}
