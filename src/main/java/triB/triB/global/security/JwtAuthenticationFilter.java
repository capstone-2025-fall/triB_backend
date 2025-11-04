package triB.triB.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.UserRepository;
import triB.triB.global.security.UserPrincipal;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

       String token = extractToken(request);

       if (token == null || token.isBlank()) {
           filterChain.doFilter(request, response);
           return;
       }

       if (jwtProvider.validateAccessToken(token)) {
           Claims claims = jwtProvider.getClaimsFromAccessToken(token);
           Long userId = jwtProvider.extractUserId(token);

           User user = userRepository.findById(userId)
                   .orElseThrow(()->new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

           UserPrincipal userPrincipal = new UserPrincipal(user, user.getUserId(), user.getEmail(), user.getUsername(), user.getPassword());
           UsernamePasswordAuthenticationToken authentication
                   = new UsernamePasswordAuthenticationToken(userPrincipal, null, null);
           authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

           SecurityContext context = SecurityContextHolder.createEmptyContext();
           context.setAuthentication(authentication);
           SecurityContextHolder.setContext(context);
           request.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

           log.debug("SecurityContext가 생성되었습니다. userId: {} ", userPrincipal.getUserId());
       } else {
           log.debug("유효한 jwt 토큰이 request에 대해 존재하지 않습니다. request: {}", request.getRequestURI());
           throw new JwtException("유효한 jwt 토큰이 request에 대해 존재하지않습니다.");
       }
       filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        return (header == null || !header.startsWith("Bearer ")) ? null : header.substring("Bearer ".length());
    }
}
