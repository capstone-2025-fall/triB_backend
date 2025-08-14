package triB.triB.global;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;

    // 성공응답 데이터 있을때
    public static <T> ResponseEntity<ApiResponse<T>> ok(HttpStatus httpStatus, String message, T data) {
        return new ResponseEntity<>(new ApiResponse<>(httpStatus.value(), message, data), httpStatus);
    }

    // 성공응답 데이터 없을때
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return new ResponseEntity<>(new ApiResponse<>(HttpStatus.OK.value(), message, null), HttpStatus.OK);
    }

    // 에러 응답
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new ApiResponse<>(httpStatus.value(), message, null), httpStatus);
    }
}
