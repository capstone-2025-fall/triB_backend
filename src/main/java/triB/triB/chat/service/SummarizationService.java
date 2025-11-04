//package triB.triB.chat.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//import triB.triB.chat.dto.GeminiRequest;
//import triB.triB.chat.dto.GeminiResponse;
//
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class SummarizationService {
//
//    @Value("${gemini.model}")
//    private String model;
//
//    @Value("${gemini.api-key}")
//    private String apiKey;
//
//    private final @Qualifier("geminiWebClient") WebClient geminiWebClient;
//
//    // todo Mono 추가하기
//    public Mono<GeminiResponse> summarizeChat(List<String> messages) {
//
//        String prompt = """
//
//                당신은 긴 채팅 로그에서 여행 선호도를 분석하고, 이를 바탕으로 여행 유형을 문장으로 요약하는 시스템입니다.
//
//                [규칙]
//                1) 의미 없는 토큰과 잡음(예: ㅋㅋ, ㅎㅎ, 이모티콘, 반복 자음·모음, 단순 반응 “ㅇㅋ”, “네”)은 분석에서 제외합니다.
//                2) 같은 의미의 메시지는 군집화하여 중복 의도를 통합하고 한 번만 반영합니다.
//                3) 대화속 핵심 의도를 가성비, 미식, 자유여행, 쇼핑 중심, 액티비티 선호, 야경 선호, 박물관 전시 선호, 여유로운 페이스와 같은 키워드로 정의하고, 이를 기반으로 여행 유형을 요약합니다.
//                3-1) [국가/도시 규칙] summarize 문장은 반드시 국가와 도시를 포함합니다.
//                    - 형식: "<국가> <도시> 여행으로, ...하고 싶다."
//                    - 국가나 도시가 여러 개인 경우, 나라에 맞는 일정으로 전부 포함합니다.
//                4) 장소, 음식, 활동 등 구체적인 선호/기피 요소를 명확히 언급하여 요약에 포함합니다.
//                5) 안전, 예산 수준, 이동 방식, 이동 동선 난이도 등의 부가 정보를 추가합니다.
//                6) 한국어 평서문으로만 출력하고, 따옴표나 부가설명 금지합니다.
//                7) 정보가 부족하면 “정보 부족으로 선호 파악이 어려움(추정: …)” 형태로 한 문장으로 답합니다.
//                8) summarize의 문장은 한국어 평서문으로만 작성하며, 따옴표나 부가설명을 넣지 않습니다.
//                9) 마지막은 "...하고 싶다", "...한 여행 스타일이다." 등 으로 끝나야 합니다.
//
//                [이동 및 일정 규칙]
//                10) “n일차”, “Day n”, “첫날/둘째날/마지막날” 등의 표현이 있으면 일정 순서를 인식합니다.
//                11) “A 갔다가 B”, “A 다음에 B”, “A→B”, “A 들렀다가 B”, “A 보고 B 간다” 등의 문장에서 장소 간 이동 관계를 추출합니다.
//                12) itinerary는 단일 문자열로 작성하며, 날짜별로 “[1일차] A 방문 후 B 이동, 이후 C”, “[2일차] …”처럼 쉼표와 공백(“, ”)으로 이어 붙입니다.
//                13) 날짜 표기가 없더라도 이동 언급이 있으면 “[계획] A 방문 후 B 이동, 이후 C” 한 줄만 작성합니다.
//                14) 일정 정보가 매우 적으면 해당되는 부분만 작성합니다. (예: “[1일차] A 방문 후 B 이동”만 있을 수 있음)
//
//                [이동 방식 판단]
//                15) travelMode는 DRIVE 또는 TRANSIT 중 하나로 결정합니다.
//                	- DRIVE: 렌터카/자차/택시/고속도로/톨게이트/주차 등 차량 관련 언급이 많을 때
//                	- TRANSIT: 지하철/버스/도보/교통패스/역명/환승 등 대중교통 언급이 많을 때
//                	- 근거가 희박할 경우 보수적으로 “TRANSIT”을 선택합니다.
//
//                [추가 분석 항목]
//                16) 대화 속에 ‘공항’, ‘비행기’, ‘출발’, ‘도착’ 등의 표현이 있으면 공항 정보를 추출하여 "details.airports" 배열에 저장합니다. (예: ["인천 국제공항(4일차 출발)", "하네다 국제공항(5일차 도착)"])
//                    - 공항명이 가시적으로 나와있지 않은 경우, 여행 장소에 있는 실제 공항명으로 추론해서 공항 정보를 저장합니다.
//                    - 공항에 도착하는 건지, 공항에서 출발하는 건지 대화를 통해 추론하여 괄호로 첫째날 도착/0일차 출발 과 같이 저장합니다.
//                17) ‘호텔’, ‘숙소’, ‘게스트하우스’, ‘에어비앤비’ 등의 표현이 있으면 확정된 숙소 정보를 추출하여 "details.accommodations" 배열에 저장합니다.
//                18) “~시에 출발”, “~시쯤 시작”, “~시까지 돌아가자”, “~시쯤 끝내자” 등의 표현이 있으면 여행 일정 시간을 추출하여 "details.schedule"의 n일차별 "start"와 "end" 필드로 정리합니다.
//                19) 여행 1일차나, 마지막 날은 비행기 출발 및 도착 시간 내용을 꼭 반영합니다.
//                20) 공항, 숙소, 시간 정보가 없으면 해당 필드는 생략 가능합니다.
//
//                [출력(JSON) 스키마]
//                아래와 정확히 동일한 키를 갖는 JSON 객체를 한 번만 출력합니다.
//                {
//                	  "summarize": "<한국어 문장들 요약, 끝맺음은 '...하고 싶다'>",
//                	  "travelMode": "DRIVE" 또는 "TRANSIT",
//                	  "itinerary": "<단일 문자열: [1일차] ..., [2일차] ..., [계획] ... 중 해당되는 항목만 쉼표+공백으로 연결>",
//                	  "details": {
//                			  "airports": ["..."],
//                        "accommodations": ["..."],
//                        "schedule": {
//                		       "1일차": {"start": "...", "end": "..."}
//                        }
//                    }
//                }
//
//                [예시]
//                입력 대화 요지: ‘첫날 경복궁과 북촌 한옥마을 방문 후 광장시장으로 이동하고, 둘째 날 국립중앙박물관 관람 뒤 남산 서울타워에서 야경을 보며, 숙소는 명동에 두고 대중교통을 주로 이용하며 인천으로 도착하고 김포에서 출발하는 일정으로 보입니다.’
//                예시 출력:
//                {
//                    "summarize": "대한민국 서울 여행으로, 가성비와 미식을 중시하며 박물관 전시와 야경을 즐기고 대중교통을 활용해 도심 명소를 여유로운 페이스로 둘러보고 싶다",
//                    "travelMode": "TRANSIT",
//                    "itinerary": "[1일차] 경복궁 방문 후 북촌 한옥마을 이동, 점심 광장시장에서 비빔밥, 저녁 냉면, [2일차] 국립중앙박물관 관람 후 남산 서울타워 야경 이동 저녁 명동 칼국수, [계획] 홍대 거리 산책 후 합정 카페 방문",
//                    "details": {
//                		"airports": ["인천국제공항(1일차 도착)", "김포국제공항(마지막날 출발)"],
//                        "accommodations": ["명동 00 호텔"],
//                        "schedule": {
//                		    "1일차": {"start": "10:00", "end": "21:30"},
//                            "2일차": {"start": "10:30", "end": "22:00"}
//                        }
//                    }
//                }
//
//                이제 아래 채팅 로그를 분석하여 위 형식대로 출력하십시오.
//
//                """;
//
//        String msg = IntStream.range(0, messages.size())
//                .mapToObj(i -> (i + 1) + ". " + messages.get(i))
//                .collect(Collectors.joining("\n"));
//
//        // Gemini 에게 보낼 request 만듦
//        GeminiRequest request = GeminiRequest.builder()
//                .systemInstruction(
//                        GeminiRequest.SystemInstruction.builder()
//                                .parts(List.of(
//                                        GeminiRequest.ofText(prompt)
//                                ))
//                                .build()
//                )
//                .contents(List.of(
//                        GeminiRequest.Content.builder()
//                                .parts(List.of(
//                                        GeminiRequest.ofText(msg)
//                                ))
//                                .build()
//                ))
//                .build();
//
//        // Gemini와 통신해야할 주소
//        String url = "/v1beta/models/" + model + ":generateContent?key=" + apiKey;
//
//        // Gemini와 통신하는 과정
//        Mono<GeminiResponse> response = geminiWebClient.post()
//                .uri(url)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .retrieve()
//                .onStatus(
//                        HttpStatusCode::is4xxClientError,
//                        res -> res.createException().flatMap(Mono::error)
//                )
//                .onStatus(
//                        HttpStatusCode::is5xxServerError,
//                        res -> res.createException().flatMap(Mono::error)
//                )
//                .bodyToMono(String.class)
//                .map(body -> {
//                    try {
//                        ObjectMapper mapper = new ObjectMapper();
//                        JsonNode root = mapper.readTree(body);
//
//                        String text = root.at("/candidates/0/content/parts/0/text").asText();
//
//                        String jsonString = text.trim()
//                                .replaceAll("```json", "") // 시작 부분의 ```json 제거
//                                .replaceAll("```", "")     // 끝 부분의 ``` 제거
//                                .trim(); // 다시 한번 앞뒤 공백 및 개행 문자 제거
//
//                        return mapper.readValue(jsonString, GeminiResponse.class);
//                    } catch (Exception e) {
//                        log.error("왜오류", e);
//                        throw new RuntimeException("Gemini 응답 파싱에 실패했습니다."); // todo 자세히 어떤에런지 요약 적어야할듯
//                    }
//                });
//
//        log.info(String.valueOf(response));
//        return response;
//    }
//
//
//}
