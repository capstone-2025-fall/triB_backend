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
import triB.triB.chat.dto.GeminiResponse;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummarizationService {

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.api-key}")
    private String apiKey;

    private final @Qualifier("geminiWebClient") WebClient geminiWebClient;

    // todo Mono 추가하기
    public Mono<GeminiResponse> summarizeChat(List<String> messages) {

        String prompt = """
                
                당신은 긴 채팅 로그에서 여행 선호도를 분석하고, 이를 바탕으로 여행 유형을 문장으로 요약하는 시스템입니다.
                
                [규칙]
                1) 의미 없는 토큰과 잡음(예: ㅋㅋ, ㅎㅎ, 이모티콘, 반복 자음·모음, 단순 반응 “ㅇㅋ”, “네”)은 분석에서 제외.
                2) 같은 의미의 메시지는 군집화하여 중복 의도를 통합하고 한 번만 반영.
                3) 대화속 핵심 의도를 가성비, 미식, 자유여행, 쇼핑 중심, 액티비티 선호, 야경 선호, 박물관 전시 선호, 여유로운 페이스와 같은 키워드로 정의하고, 이를 기반으로 여행 유형을 요약.
                3-1) [국가/도시 규칙] summarize 문장은 반드시 국가와 도시를 포함한다.
                	- 형식: "<국가> <도시> 여행으로, ...하고 싶다."
                  - 국가나 도시가 여러 개인 경우 전부 포함한다.
                4) 장소, 음식, 활동 등 구체적인 선호/기피 요소를 명확히 언급하여 요약에 포함.
                5) 안전, 예산 수준, 이동 방식, 이동 동선 난이도 등의 부가 정보를 추가.
                6) 한국어 평서문으로만 출력. 따옴표나 부가설명 금지
                7) 정보가 부족하면 “정보 부족으로 선호 파악이 어려움(추정: …)” 형태로 한 문장으로 답변.
                8) summarize의 문장은 한국어 평서문으로만 작성하며, 따옴표나 부가설명을 넣지 않습니다.
                9) 마지막은 "...하고 싶다", "...한 여행 스타일이다." 등 으로 끝나야 합니다.
                
                [이동 및 일정 규칙]
                10) “n일차”, “Day n”, “첫날/둘째날/마지막날” 등의 표현이 있으면 일정 순서를 인식합니다.
                11) “A 갔다가 B”, “A 다음에 B”, “A→B”, “A 들렀다가 B”, “A 보고 B 간다” 등의 문장에서 장소 간 이동 관계를 추출합니다.
                12) itinerary는 단일 문자열로 작성하며, 날짜별로 “[1일차] A 방문 후 B 이동, 이후 C”, “[2일차] …”처럼 쉼표와 공백(“, ”)으로 이어 붙입니다.
                13) 날짜 표기가 없더라도 이동 언급이 있으면 “[계획] A 방문 후 B 이동, 이후 C” 한 줄만 작성합니다.
                14) 일정 정보가 매우 적으면 해당되는 부분만 작성합니다. (예: “[1일차] A 방문 후 B 이동”만 있을 수 있음)
                
                [이동 방식 판단]
                15) travelMode는 DRIVE 또는 TRANSIT 중 하나로 결정합니다.
                	- DRIVE: 렌터카/자차/택시/고속도로/톨게이트/주차 등 차량 관련 언급이 많을 때
                	- TRANSIT: 지하철/버스/도보/교통패스/역명/환승 등 대중교통 언급이 많을 때
                	- 근거가 희박할 경우 보수적으로 “TRANSIT”을 선택합니다.
                
                [추가 분석 항목]
                16) 대화 속에 ‘공항’, ‘비행기’, ‘출발’, ‘도착’ 등의 표현이 있으면 공항 정보를 추출하여 "details.airports" 배열에 저장합니다. (예: ["파리공항(4일차)", "로마공항(5일차)"])
                17) ‘호텔’, ‘숙소’, ‘게스트하우스’, ‘에어비앤비’ 등의 표현이 있으면 확정된 숙소 정보를 추출하여 "details.accommodations" 배열에 저장합니다.
                18) “~시에 출발”, “~시쯤 시작”, “~시까지 돌아가자”, “~시쯤 끝내자” 등의 표현이 있으면 여행 일정 시간을 추출하여 "details.schedule"의 n일차별 "start"와 "end" 필드로 정리합니다.
                19) 여행 1일차나, 마지막날은 비행기 및 출발 도착 시간 내용을 꼭 반영합니다.
                20) 공항, 숙소, 시간 정보가 없으면 해당 필드는 생략 가능합니다.
                
                [출력(JSON) 스키마]
                아래와 정확히 동일한 키를 갖는 JSON 객체를 한 번만 출력합니다.
                {
                		"summarize": "<한국어 문장들 요약, 끝맺음은 '...하고 싶다'>",
                	  "travelMode": "DRIVE" 또는 "TRANSIT",
                	  "itinerary": "<단일 문자열: [1일차] ..., [2일차] ..., [계획] ... 중 해당되는 항목만 쉼표+공백으로 연결>",
                	  "details": {
                			  "airports": ["..."],
                        "accommodations": ["..."],
                        "schedule": {
                		        "1일차": {"start": "...", "end": "..."}
                        }
                    }
                }
                
                [예시]
                입력 대화 요지: ‘첫날 오사카성 후 도톤보리, 셋째 날 규카츠 맛집과 아쿠아리움, 대중교통 언급 다수’
                예시 출력:
                {
                		"summarize": "일본 오사카 여행으로, 가성비와 미식을 중시하며 도심 위주로 대중교통을 이용해 오사카성과 도톤보리, 아쿠아리움 등을 여유로운 페이스로 둘러보고 싶다",
                    "travelMode": "TRANSIT",
                    "itinerary": "[1일차] 오사카성 방문 후 도톤보리 이동, [3일차] 규카츠 맛집 방문 후 아쿠아리움 이동",
                    "details": {
                		    "airports": ["오사카 공항(1일차/3일차)"],
                        "accommodations": ["000 아파트"],
                        "schedule": {
                		        "1일차": {"start": "10:00", "end": "22:00"},
                            "3일차": {"start": "10:00", "end": "16:00"},
                        }
                    }
                }
                
                이제 아래 채팅 로그를 분석하여 위 형식대로 출력하십시오.
                
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
        Mono<GeminiResponse> response = geminiWebClient.post()
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
                        ObjectMapper mapper = new ObjectMapper();

                        JsonNode root = mapper.readTree(body);
                        String text = root.at("/candidates/0/content/parts/0/text").asText();

                        return mapper.readValue(text, GeminiResponse.class);
                    } catch (Exception e) {
                        log.error("왜오류", e);
                        throw new RuntimeException("Gemini 응답 파싱에 실패했습니다."); // todo 자세히 어떤에런지 요약 적어야할듯
                    }
                });

        log.info(String.valueOf(response));
        return response;
    }


}
