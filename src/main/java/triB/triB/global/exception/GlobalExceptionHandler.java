package triB.triB.global.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import triB.triB.global.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 요청 검증(@Valid) 실패/바인딩 실패 공통 처리
     */
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception ex) {
        String msg = (ex instanceof MethodArgumentNotValidException manve)
                ? manve.getBindingResult().getAllErrors().get(0).getDefaultMessage()
                : ((BindException) ex).getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.fail(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                msg
        );
    }

    /**
     * 엔티티 미존재(404) 처리
     * Optional 가져올때 orElseThrow(EntityNotFoundException::new) 이용
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ApiResponse.fail(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                ex.getMessage() != null ? ex.getMessage() : "리소스를 찾을 수 없습니다."
        );
    }

    /**
     * 비즈니스 로직 일반 실패(400) 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        return ApiResponse.fail(
                HttpStatus.BAD_REQUEST,
                "BUSINESS_ERROR",
                ex.getMessage()
        );
    }

    /**
     * 인증 실패(401) 처리
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        return ApiResponse.fail(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                ex.getMessage()
        );
    }

    /**
     * JWT 만료(401) 처리
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(ExpiredJwtException ex) {
        return ApiResponse.fail(
                HttpStatus.UNAUTHORIZED,
                "JWT_EXPIRED",
                ex.getMessage()
        );
    }

    /**
     * JWT 일반 오류 처리(401)
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(JwtException ex) {
        return ApiResponse.fail(
                HttpStatus.UNAUTHORIZED,
                "INVALID_JWT",
                ex.getMessage()
        );
    }

    /**
     * 데이터 무결성 위반(409) 처리
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ApiResponse.fail(
                HttpStatus.CONFLICT,
                "CONFLICT",
                ex.getMessage() != null ? ex.getMessage() : "이미 사용 중인 값입니다."
        );
    }

    /**
     * 마지막 안정망(500) 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        return ApiResponse.fail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                ex.getMessage() != null ? ex.getMessage() : "예상치 못한 오류가 발생했습니다."
        );
    }
}
