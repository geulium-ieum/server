package seg.work.geuliumieum.server.config.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import seg.work.geuliumieum.server.config.security.UserRole;
import seg.work.geuliumieum.server.user.dto.UserInfo;
import seg.work.geuliumieum.server.util.RedisUtil;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        MDC.put("traceId", String.valueOf(System.nanoTime()));
        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                try {
                    if (jwtTokenProvider.isValid(token)) {
                        Claims claims = jwtTokenProvider.parseClaims(token);
                        String jti = claims.getId();
                        if (!isBlacklisted(jti)) {
                            Long userId = Optional.ofNullable(claims.get("uid", Number.class)).map(Number::longValue).orElse(null);
                            String role = Optional.ofNullable(claims.get("role", String.class)).orElse("USER");
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(UserInfo.builder()
                                .id(userId)
                                .email(claims.getSubject())
                                .name(claims.get("name", String.class))
                                .role(UserRole.valueOf(role))
                                .token(token)
                                .build(), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                            authentication.setDetails(userId);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                } catch (Exception ignored) {
                    // Invalid token
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isBlacklisted(String jti) {
        if (jti == null) {
            return false;
        }
        String key = "bl:access:" + jti;
        return RedisUtil.has(key);
    }
}
