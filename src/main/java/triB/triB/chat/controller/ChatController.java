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
                "야 이번 방학 이탈리아 로마 ㄱ?",
                "콜로세움이랑 포로 로마노는 기본으로",
                "바티칸 박물관 줄 2시간은 좀 에바…",
                "ㅇㅋ 예매없으면 가지말자 ㄱㄱ",
                "성당은 산피에트로만 가자",
                "ㅇㅈ 성당 투어 이런거 개오바임",
                "트레비 분수 밤 감성 찍자",
                "판테온은 꼭 가봐야지",
                "진실의 입도 가야지 인증샷 찍어야 되는데",
                "스페인 광장 계단에서 젤라또 먹자!",
                "파스타·젤라또 꼭 먹자!!!",
                "미슐랭 에바임 돈없음",
                "하루 15km 이상 걷는 건 좀.. 버스 타자",
                "야 택시 안됨 개비쌈",
                "숙소는 테르미니 역 근처 가성비 찾아봄 ㄱㄷ",
                "야 €60 이상은 안됨 그거 이하로만 ㄱ",
                "와 근데 가서 음식 ㅈㄴ뿌수자",
                "유명한 햄버거집 있다던데?",
                "가실?ㅋㅋ",
                "야 근데 니네 거기서 한식 찾지마셈 개오바임",
                "이번 여행 완전 유럽 즐기러 가는건데 뭔 한식이여",
                "사진 ㅈㄴ많이찍어야지 이쁜 옷 입고",
                "아 옷사야겠닼ㅋ",
                "야 유럽 소매치기 많으니까 가방 소매치기 방지용으로 사셈",
                "얘드라 해산물은 많이먹지말자 ㅎ",
                "아 근데 여긴 꼭가야됨 000식당 여기 ㅈㄴ맛있댔음",
                "진정해",
                "피자 맛집도 찾아볼까?",
                "봉골레 파스타 먹고싶다",
                "티라미수 본토에서 먹으면 다르다던데",
                "트라스테베레 지구 야경 좋대",
                "거기서 맥주 한잔?",
                "콜로세움 야간 투어도 할까?",
                "야간 투어는 좀 비싸지 않나?",
                "그냥 낮에 봐도 충분할듯",
                "숙소는 역 근처가 짱이지",
                "근데 치안은 괜찮을까?",
                "로마 치안 괜찮대 걱정마",
                "나 폼페이도 가고싶은데?",
                "폼페이 당일치기 ㄱ?",
                "너무 빡세지 않나? 로마만 돌아도 충분할거 같은데",
                "야 이탈리아 남부 갈거야?",
                "남부까지는 에바지.. 그냥 로마에 집중하자",
                "로마 시내 버스 노선 미리 봐놓자",
                "버스비 얼마려나?",
                "로마패스 사는게 이득일까?",
                "패스 비싸면 그냥 버스표 끊자",
                "환전은 미리 해갈까?",
                "현지 ATM에서 뽑는게 낫대",
                "트레비 분수에 동전 던지고 와야지",
                "한식당 진짜 한군데도 안갈거야?",
                "야 진짜 오지마라 한식당 얘기 꺼내지도 마셈",
                "로마에 000버거집 꼭 가야된대",
                "거기 줄 길다던데?",
                "그래도 꼭 먹어보고 싶어",
                "젤라또는 1일 1젤라또다",
                "야 젤라또집 추천해줘",
                "파시(Fassi)가 유명하다던데",
                "기념품은 뭐 사오지?",
                "마비스 치약이 유명하대",
                "이탈리아 명품 브랜드도 구경가자",
                "돈 없잖아.. 눈으로만 보자",
                "카르보나라 본토 맛 궁금",
                "000식당은 무조건 웨이팅 길겠지?",
                "예약하고 가야되나",
                "야 로마 시내에 스타벅스 별로 없대",
                "오 신기하다ㅋㅋ",
                "카푸치노랑 에스프레소 마셔야지",
                "아침에 샌드위치 사서 먹자",
                "숙소에서 아침 해결하면 돈 아끼지",
                "근데 밤에 위험한 곳은 없어?",
                "구글맵으로 확인하고 다니자",
                "폰 배터리 넉넉하게 준비해",
                "보조배터리 필수",
                "로마 시내 돌아다니면서 길거리 음식도 먹자",
                "피자 알 탈리오 먹어야지",
                "콜로세움 앞 000 피자집 유명하대",
                "거기 갈까?",
                "가자! 거기 웨이팅 얼마나 되려나",
                "로마 4대 성당 투어는 진짜 안할거지?",
                "응 진짜 안해",
                "야 포토존도 많이 찾아놔라",
                "트레비 분수 명당 자리 있대",
                "거기서 사진 100장 찍자",
                "옷은 최대한 편하게",
                "근데 사진 잘 나오게 이쁜 옷도 챙겨야지",
                "캐리어 꽉 채워가야겠다",
                "야 기념품은 나중에 사",
                "무겁잖아",
                "로마 공항에서 시내까지 어떻게 가지?",
                "버스나 기차 타면 된대",
                "택시는 비싸니까 패스",
                "로마 여행 후기 ㅈㄴ 찾아보고 있음",
                "우리만 그런거 아니지?",
                "다들 이러지 뭐 ㅋㅋ",
                "여행 준비하는게 제일 재밌는듯"
        );

        String response = summarizationService.summarizeChat(messages).block();
        return ResponseEntity.ok(response);
    }
}
