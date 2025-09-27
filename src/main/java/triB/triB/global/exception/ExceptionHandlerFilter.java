package triB.triB.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.filter.OncePerRequestFilter;
import triB.triB.global.response.ApiResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (EntityNotFoundException e){
            log.error(e.getMessage());
            setErrorResponse(response, HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
        } catch(ExpiredJwtException e){
            log.error(e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "JWT_EXPIRED", e.getMessage());
        }
            catch (JwtException e){
            log.error(e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED,"INVALID_JWT", e.getMessage());
        }
    }

    public void setErrorResponse(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {

        if (response.isCommitted())
            return;
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("code", code);
        body.put("message", message);
        body.put("data", null);
        body.put("timestamp", Instant.now().toString());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
