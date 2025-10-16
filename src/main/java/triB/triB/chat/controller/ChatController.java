package triB.triB.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<String> testGemini(){
        List<String> messages = List.of(
                "오사카엔 뭐가 유명하대?",
                "오사카가면 무조건 유니버설 스튜디오 가야돼",
                "ㅇㅇ 그건 무조건이지ㅋㅋ 유니버설 빼면 오사카 간 거 아님",
                "ㄱㄱㄱㄱㄱㄱㄱㄱ 바로 일정 짜자",
                "첫날은 도착하니까 오사카성 정도만 가자. 무리 ㄴㄴ",
                "ㅇㅋ 첫날은 그냥 가볍게 관광 느낌으로",
                "그럼 오사카성 갔다가 도톤보리에서 야경 보자~",
                "좋아! 도톤보리 간판 밑에서 사진 찍자ㅋㅋ",
                "그 글리코상 앞에서 점프샷 가야지",
                "ㅋㅋㅋ 인스타각이네 완전",
                "근데 오사카에 아쿠아리움도 있던데?",
                "오 거기 고래상어 있다며? 가야지",
                "마지막날에 딱 들렀다가 공항 가면 딱이네",
                "ㄹㅇ 깔끔하다. 오사카성-유니버설-아쿠아리움 완벽",
                "둘째 날은 유니버설 하루 종일이지?",
                "ㅇㅇ 오픈런해서 폐장까지 존버 각임ㅋㅋ",
                "그럼 숙소는 도톤보리 근처로 잡자. 교통 편해",
                "좋지! 밤에 돌아다니기도 편하고 맛집 많고",
                "그럼 숙소 내가 알아볼게, 게하 괜찮음?",
                "게하 좋지ㅋㅋ 일본 감성 느껴보자",
                "근데 음식 뭐 먹을래? 난 오코노미야키 꼭 먹고 싶음",
                "오 나도!! 부타만도 먹고 싶어. 돼지고기 찐빵이라며?",
                "ㅇㅇ 현지에서 먹으면 찐맛이라던데",
                "그럼 첫날 점심 오코노미야키, 저녁 부타만 ㄱㄱ",
                "둘째 날은 유니버설 안에서 미니언 푸드 먹자ㅋㅋ",
                "ㅋㅋㅋ 귀엽지 그거",
                "셋째 날은 회전초밥 먹고 아쿠아리움 가면 깔끔하네",
                "좋다. 그럼 일식 위주로만 먹자. 한식 금지ㅋㅋ",
                "ㄹㅇ 일본 가서 김치찌개는 좀 아니지",
                "도톤보리 가면 타코야키도 먹자. 오사카는 타코야키지",
                "타코야키 무조건. 치즈 들어간 거 꼭 먹자ㅋㅋ",
                "아 그리고 신사이바시 거리 쇼핑도 넣자",
                "오케이 둘째 날 밤에 쇼핑 ㄱㄱ",
                "유니버설 끝나고 화장품이랑 기념품 좀 사야겠다",
                "나 디즈니 굿즈 살래ㅋㅋ",
                "ㅋㅋㅋ 나 포켓몬 센터도 들를래",
                "그럼 교통패스도 알아봐야겠다. 오사카 주유패스 좋대",
                "응 하루 무제한이라더라. 2일권 사자",
                "좋네. 환전은 내가 알아볼게",
                "티켓은 내가 예매할게, 온라인이 싸대",
                "그럼 역할 분담 끝ㅋㅋ",
                "비 오면 우메다 스카이빌딩 가자, 야경 미쳤대",
                "오 야경충 나 부른거지ㅋㅋ",
                "ㅋㅋㅋㅋ 사진 각이야 진짜",
                "근데 예산 얼마쯤 잡을까?",
                "숙소 2박 6만엔 정도, 교통이랑 식비 다 합쳐서 10만엔쯤?",
                "좋다, 카드 긁자ㅋㅋ",
                "ㅋㅋㅋ 여행은 돈쓰러 가는거잖아",
                "아 맞다 첫날 신사 하나 들르자, 텐만구 신사 예쁨",
                "좋아, 거기 가서 사진 몇 장 찍고 바로 오사카성 가면 되겠다",
                "ㅇㅋㅇㅋ 일정 완성됐다ㅋㅋ",
                "진짜 가는 거 실화냐ㅋㅋ 개설렌다"
                );

        String response = summarizationService.summarizeChat(messages).block();
        return ResponseEntity.ok(response);
    }
}
