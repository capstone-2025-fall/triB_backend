package triB.triB.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triB.triB.chat.dto.GeminiResponse;
import triB.triB.chat.service.ChatService;
import triB.triB.chat.service.SummarizationService;
import triB.triB.global.response.ApiResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SummarizationService summarizationService;

    @PostMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Map<String, List<Long>>>> createItinerary(@PathVariable Long roomId){
        List<Long> trips = chatService.makeTrips(roomId);
        Map<String, List<Long>> map = new HashMap<>();
        map.put("tripIds", trips);
        return ApiResponse.created("일정을 생성했습니다.", map);
    }

    @PostMapping("/test")
    public ResponseEntity<GeminiResponse> testGemini(){
        List<String> messages = List.of(
                "후쿠오카 뭐가 유명하지",
                "거울의바다!!",
                "거울의 바다 꼭가야됨",
                "엥 그게 뭐임",
                "해변가인데",
                "바다가 엄청 투명해서 거울처럼 보임",
                "완전 이뻐",
                "오 ㅇㅋ",
                "나카스 포장마차 거리도 가보고싶어",
                "오 저녁에 가면 좋겠다",
                "쇼핑은 어디서 해?",
                "캐널시티 하카타라고",
                "복합 문화시설 있던데",
                "거기서 쇼핑하면 될듯? 뭐 많아보이더라",
                "좋아용",
                "뱃놀이 안 하실?",
                "오 그런게 있어?",
                "웅 유명한거 2개 있던데",
                "야나가와 뱃놀이? 하고 나카스 하카타부네? 일케 2개",
                "둘중에 하나 가까운 데로 가면 좋을듯!",
                "ㄱㄱ",
                "우리 신사도 가?",
                "사실 난 신사는 별로 안 땡겨",
                "신사 갈 시간에 밥먹을거임",
                "하루 4끼 ㄱㄱ",
                "먹을거면 일본식으로 먹자",
                "최대한 메뉴 안 겹치게 ㄱㄱ",
                "좋아 나 라멘 진짜 좋아함",
                "후쿠오카가 돈코츠 라멘 원조라며?",
                "맞아! 하카타 라멘 꼭 먹어야지",
                "이치란 본점도 거기 있대",
                "오 거기 줄 엄청 길다던데ㅋㅋ",
                "새벽에 가면 좀 덜 기다린대",
                "ㅋㅋㅋ 새벽 라멘 각?",
                "가능ㅋㅋ 여행 때는 밤낮 없음",
                "나 또 멘타이코도 먹고 싶어",
                "오 맞아 명란젓 유명하지 거기",
                "밥이랑 같이 먹으면 진짜 맛있겠다",
                "그럼 첫날은 라멘, 둘째날은 멘타이코 정식 어때?",
                "완벽ㅋㅋ",
                "근데 숙소 어디로 잡을까?",
                "하카타역 근처가 낫지 않을까?",
                "ㅇㅇ 교통도 좋고 포장마차 거리도 가까워",
                "그럼 내가 숙소 알아볼게!",
                "좋아 나는 맛집 담당할게ㅋㅋ",
                "둘째날 거울의바다 보러 가자",
                "그거 이토시마 쪽에 있지?",
                "응 이토시마! 사진 미쳤대 진짜",
                "그럼 아침 일찍 가야겠다",
                "맞아, 물 빠지기 전에 가야 거울처럼 보인대",
                "일출 시간 맞춰가면 최고겠다",
                "오케이 그럼 일찍 자야겠다ㅋㅋ",
                "뱃놀이하고 근처에서 덮밥 먹자",
                "덮밥은 찬성이지ㅋㅋ",
                "근데 캐널시티 안에 분수쇼도 있다던데?",
                "오 그거 밤에 보면 예쁘대",
                "교통은 후쿠오카 시티패스 같은 거 있어?",
                "응 버스+지하철 하루권 있대. 그거 사면 됨",
                "좋아 그럼 첫날 사서 바로 쓰자",
                "나중에 택시도 한 번 타보자 일본 택시 궁금해ㅋㅋ",
                "비싸다던데ㅋㅋ 그래도 한 번은 타보자",
                "그래그래 경험이지 뭐",
                "근데 야경은 어디서 보냐?",
                "후쿠오카 타워 있잖아, 거기 꼭대기에서 보는 야경 예쁘대",
                "야 거기근처에 모모치해변 있대",
                "와 거기 ㅈㄴ이쁘던데",
                "그럼 야경 전에ㄱㄱ",
                "좋다! 사진 찍어",
                "돌아가는날 공항 근처 온천ㅇㄸ?",
                "오 몸 따숩겟다 ㄱㄱ",
                "하루종일 먹고 놀고",
                "후쿠오카 먹투어 ㄹㅇ 인정",
                "다들 살쪄서 돌아올 듯ㅋㅋ",
                "ㅋㅋ 그래도 행복하면 됐지 뭐, 여행은 살찌러 가는 거잖아"
                );

        GeminiResponse response = summarizationService.summarizeChat(messages).block();
        return ResponseEntity.ok(response);
    }
}
