package triB.triB.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    CONFLICT_FRIENDSHIP_TO_ME(HttpStatus.CONFLICT, "CONFLICT_FRIENDSHIP_TO_ME","상대가 이미 보낸 친구 요청이 대기 중입니다. 수신함에서 확인하세요."),
    CONFLICT_FRIENDSHIP_TO_OTHER(HttpStatus.CONFLICT, "CONFLICT_FRIENDSHIP_TO_OTHER", "이미 친구요청을 보낸 유저입니다."),

    NO_FCM_TOKEN(HttpStatus.UNAUTHORIZED, "NO_FCM_TOKEN", "FCM 토큰이 저장되지 않았습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "refresh token이 유효하지 않습니다."),

    MODEL_REQUEST_ERROR(HttpStatus.BAD_REQUEST, "MODEL_REQUEST_ERROR", "일정 생성 요청이 잘못되었습니다."),
    MODEL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MODEL_ERROR", "일정 생성 모델에 일시적인 문제가 발생했습니다."),
    TRIP_SAVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "TRIP_SAVE_FAIL", "일정 저장 중 오류가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
