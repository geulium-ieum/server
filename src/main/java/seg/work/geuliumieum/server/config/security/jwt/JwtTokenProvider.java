package seg.work.geuliumieum.server.config.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public static final long ACCESS_TOKEN_TTL_MILLIS = 6L * 60 * 60 * 1000;
    public static final long REFRESH_TOKEN_TTL_MILLIS = 90L * 24 * 60 * 60 * 1000;

    private final SecretKey secretKey;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT 비밀키가 설정되어 있지 않습니다. 환경변수 또는 설정 파일에 jwt.secret 값을 지정해 주세요.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        Instant issuedAt = Instant.ofEpochMilli(now);
        Instant expiry = Instant.ofEpochMilli(now + ACCESS_TOKEN_TTL_MILLIS);
        return Jwts.builder()
            .header().type("JWT").and()
            .id(UUID.randomUUID().toString())
            .subject(subject)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiry))
            .claims(claims)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    public String createRefreshToken(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        Instant issuedAt = Instant.ofEpochMilli(now);
        Instant expiry = Instant.ofEpochMilli(now + REFRESH_TOKEN_TTL_MILLIS);
        return Jwts.builder()
            .header().type("JWT").and()
            .id(UUID.randomUUID().toString())
            .subject(subject)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiry))
            .claims(claims)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return false;
        }
    }
}
