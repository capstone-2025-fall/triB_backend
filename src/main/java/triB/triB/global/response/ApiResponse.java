package triB.triB.global.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private final boolean success;
    private int status;
    private String message;
    private String code;
    private T data;
    private final OffsetDateTime timestamp;   // 응답 시각

    private static <T> ResponseEntity<ApiResponse<T>> wrap(HttpStatus httpStatus,
                                                           String code,
                                                           String message,
                                                           T data) {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .success(httpStatus.is2xxSuccessful())
                .status(httpStatus.value())
                .message(message)
                .code(code)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
        return new ResponseEntity<>(body, httpStatus);
    }

    // ===================== 성공 응답 ==========================

    // 성공 200 OK
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return wrap(HttpStatus.OK, "OK", message, data);
    }

    // 성공 201 CREATED
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return wrap(HttpStatus.CREATED, "CREATED", message, data);
    }

    // 성공 204 NO CONTENT
    public static <T> ResponseEntity<ApiResponse<T>> noContent(String message) {
        return wrap(HttpStatus.NO_CONTENT, "NO_CONTENT", message, null);
    }

    // ====================== 실패 응답 =========================

    public static ResponseEntity<ApiResponse<Void>> fail(HttpStatus status,
                                                         String code,
                                                         String message) {
        return wrap(status, code, message, null);
    }
}
