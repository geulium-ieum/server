package seg.work.geuliumieum.server.auth.service;

import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import seg.work.geuliumieum.server.auth.dto.FindIdRequest;
import seg.work.geuliumieum.server.auth.dto.FindIdResponse;
import seg.work.geuliumieum.server.auth.dto.LoginRequest;
import seg.work.geuliumieum.server.auth.dto.LogoutRequest;
import seg.work.geuliumieum.server.auth.dto.MessageResponse;
import seg.work.geuliumieum.server.auth.dto.PasswordResetRequest;
import seg.work.geuliumieum.server.auth.dto.PasswordResetVerifyRequest;
import seg.work.geuliumieum.server.auth.dto.RefreshRequest;
import seg.work.geuliumieum.server.auth.dto.RegisterRequest;
import seg.work.geuliumieum.server.auth.dto.ResendVerificationRequest;
import seg.work.geuliumieum.server.auth.dto.TokenResponse;
import seg.work.geuliumieum.server.auth.dto.VerifyEmailRequest;
import seg.work.geuliumieum.server.common.entity.User;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.mail.MailClient;
import seg.work.geuliumieum.server.common.repository.UserRepository;
import seg.work.geuliumieum.server.config.security.UserRole;
import seg.work.geuliumieum.server.config.security.jwt.JwtTokenProvider;
import seg.work.geuliumieum.server.user.dto.UserInfo;
import seg.work.geuliumieum.server.util.RedisUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final MailClient mailClient;

    private static final String RT_KEY_PREFIX = "rt:user:";
    private static final String BL_ACCESS_PREFIX = "bl:access:";

    private static final String EV_CODE_PREFIX = "ev:code:";
    private static final String EV_TRIES_PREFIX = "ev:tries:";
    private static final String EV_RESEND_COOLDOWN_PREFIX = "ev:cooldown:";

    private static final String PR_CODE_PREFIX = "pf:code:";
    private static final String PR_TRIES_PREFIX = "pf:tries:";
    private static final String PR_COOLDOWN_PREFIX = "pf:cooldown:";

    @Value("${auth.email.verification.url}")
    private String emailVerificationUrl;
    @Value("${auth.email.verification.maxRetry}")
    private int emailVerificationMaxRetry;
    @Value("${auth.email.verification.ttl}")
    private int verificationTtlMinutes;
    @Value("${auth.email.verification.coolDown}")
    private int verificationResendCooldownSeconds;

    @Value("${auth.password.reset.url}")
    private String passwordResetUrl;
    @Value("${auth.password.reset.maxRetry}")
    private int passwordResetMaxRetry;
    @Value("${auth.password.reset.ttl}")
    private int passwordResetTtlMinutes;
    @Value("${auth.password.reset.coolDown}")
    private int passwordResetResendCooldownSeconds;

    public MessageResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ApiException(ErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        User user = new User();
        user.setEmail(email);
        user.setPwd(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        user.setRole(UserRole.USER);
        user.setIsActive(false);

        user = userRepository.save(user);

        String code = UUID.randomUUID().toString();
        RedisUtil.setWithExpiryMin(EV_CODE_PREFIX + email, code, verificationTtlMinutes);
        RedisUtil.delete(EV_TRIES_PREFIX + email);

        mailClient.sendVerificationEmail(email, user.getName(), emailVerificationUrl, code, verificationTtlMinutes);

        return new MessageResponse("인증 이메일이 발송되었습니다. 계정을 활성화하려면 이메일 인증을 완료해 주세요.");
    }

    public TokenResponse verifyEmail(VerifyEmailRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        String stored = RedisUtil.getStringValue(EV_CODE_PREFIX + email);
        if (stored == null) {
            throw new ApiException(ErrorCode.VERIFICATION_CODE_EXPIRED, ErrorCode.VERIFICATION_CODE_EXPIRED.getDefaultMessage());
        }

        Long tries = RedisUtil.getLongValue(EV_TRIES_PREFIX + email);
        if (tries != null && tries >= emailVerificationMaxRetry) {
            throw new ApiException(ErrorCode.VERIFICATION_TOO_MANY_ATTEMPTS, ErrorCode.VERIFICATION_TOO_MANY_ATTEMPTS.getDefaultMessage());
        }

        if (!stored.equals(request.getCode())) {
            long inc = (tries == null ? 1 : tries + 1);
            RedisUtil.setWithExpiryMin(EV_TRIES_PREFIX + email, String.valueOf(inc), verificationTtlMinutes);
            throw new ApiException(ErrorCode.VERIFICATION_CODE_INVALID, ErrorCode.VERIFICATION_CODE_INVALID.getDefaultMessage());
        }

        user.setIsActive(true);
        userRepository.save(user);
        RedisUtil.delete(EV_CODE_PREFIX + email);
        RedisUtil.delete(EV_TRIES_PREFIX + email);

        Map<String, Object> claims = defaultClaims(user);
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), claims);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), claims);
        RedisUtil.setWithExpiryMs(RT_KEY_PREFIX + user.getId(), refreshToken, JwtTokenProvider.REFRESH_TOKEN_TTL_MILLIS);

        return TokenResponse.builder()
            .tokenType("Bearer")
            .accessToken(accessToken)
            .accessTokenExpiresIn(JwtTokenProvider.ACCESS_TOKEN_TTL_MILLIS)
            .refreshToken(refreshToken)
            .refreshTokenExpiresIn(JwtTokenProvider.REFRESH_TOKEN_TTL_MILLIS)
            .build();
    }

    public void resendVerification(ResendVerificationRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new ApiException(ErrorCode.ALREADY_VERIFIED);
        }
        String cooldownKey = EV_RESEND_COOLDOWN_PREFIX + email;
        if (RedisUtil.has(cooldownKey)) {
            throw new ApiException(ErrorCode.VERIFICATION_RESEND_COOLDOWN, ErrorCode.VERIFICATION_RESEND_COOLDOWN.getDefaultMessage());
        }

        String code = UUID.randomUUID().toString();
        RedisUtil.setWithExpiryMin(EV_CODE_PREFIX + email, code, verificationTtlMinutes);
        RedisUtil.delete(EV_TRIES_PREFIX + email);
        RedisUtil.setWithExpirySec(cooldownKey, "1", verificationResendCooldownSeconds);

        mailClient.sendVerificationEmail(email, user.getName(), emailVerificationUrl, code, verificationTtlMinutes);
    }

    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new ApiException(ErrorCode.USER_NOT_VERIFIED);
        }

        Map<String, Object> claims = defaultClaims(user);
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), claims);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), claims);

        RedisUtil.setWithExpiryMs(RT_KEY_PREFIX + user.getId(), refreshToken, JwtTokenProvider.REFRESH_TOKEN_TTL_MILLIS);

        return TokenResponse.builder()
            .tokenType("Bearer")
            .accessToken(accessToken)
            .accessTokenExpiresIn(JwtTokenProvider.ACCESS_TOKEN_TTL_MILLIS)
            .refreshToken(refreshToken)
            .refreshTokenExpiresIn(JwtTokenProvider.REFRESH_TOKEN_TTL_MILLIS)
            .build();
    }

    public TokenResponse refresh(UserInfo userInfo, RefreshRequest request) {
        String refreshToken = StringUtils.hasText(request.getRefreshToken()) ? request.getRefreshToken() : userInfo.getToken();
        if (!StringUtils.hasText(refreshToken) || !jwtTokenProvider.isValid(refreshToken)) {
            throw new ApiException(ErrorCode.JWT_INVALID_TOKEN);
        }

        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        Long userId = Optional.ofNullable(claims.get("uid", Number.class)).map(Number::longValue).orElse(null);
        if (userId == null) {
            throw new ApiException(ErrorCode.JWT_INVALID_TOKEN);
        }

        String key = RT_KEY_PREFIX + userId;
        String stored = RedisUtil.getStringValue(key);
        if (!refreshToken.equals(stored)) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        User user = userRepository.findById(userId).orElseThrow();
        Map<String, Object> newClaims = defaultClaims(user);
        String newAccess = jwtTokenProvider.createAccessToken(user.getEmail(), newClaims);
        String newRefresh = jwtTokenProvider.createRefreshToken(user.getEmail(), newClaims);

        RedisUtil.setWithExpiryMs(key, newRefresh, JwtTokenProvider.REFRESH_TOKEN_TTL_MILLIS);

        return TokenResponse.builder()
            .tokenType("Bearer")
            .accessToken(newAccess)
            .accessTokenExpiresIn(JwtTokenProvider.ACCESS_TOKEN_TTL_MILLIS)
            .refreshToken(newRefresh)
            .refreshTokenExpiresIn(JwtTokenProvider.REFRESH_TOKEN_TTL_MILLIS)
            .build();
    }

    public void logout(UserInfo userInfo, LogoutRequest request) {
        if (StringUtils.hasText(userInfo.getToken())) {
            try {
                Claims ac = jwtTokenProvider.parseClaims(userInfo.getToken());
                long ttlMs = ttlMsFromClaims(ac);
                if (ttlMs > 0) {
                    RedisUtil.setWithExpiryMs(BL_ACCESS_PREFIX + ac.getId(), "1", ttlMs);
                }
            } catch (Exception ignore) {
            }
        }

        if (!StringUtils.hasText(request.getRefreshToken())) {
            if (StringUtils.hasText(userInfo.getToken())) {
                try {
                    Claims ac = jwtTokenProvider.parseClaims(userInfo.getToken());
                    Optional.ofNullable(ac.get("uid", Number.class)).map(Number::longValue).ifPresent(userId -> RedisUtil.delete(RT_KEY_PREFIX + userId));
                } catch (Exception ignore) {
                }
            }
        } else {
            try {
                Claims rc = jwtTokenProvider.parseClaims(request.getRefreshToken());
                Optional.ofNullable(rc.get("uid", Number.class)).map(Number::longValue).ifPresent(userId -> RedisUtil.delete(RT_KEY_PREFIX + userId));
            } catch (Exception ignore) {
            }
        }
    }

    private long ttlMsFromClaims(Claims claims) {
        Instant exp = claims.getExpiration().toInstant();
        long ttl = exp.toEpochMilli() - System.currentTimeMillis();
        return Math.max(ttl, 0);
    }

    private Map<String, Object> defaultClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", user.getId());
        claims.put("role", user.getRole() == null ? "USER" : user.getRole().name());
        claims.put("name", user.getName());
        return claims;
    }

    public FindIdResponse findId(FindIdRequest request) {
        String name = request.getName().trim();
        String phone = request.getPhone().trim();
        Optional<User> opt = userRepository.findByNameAndPhone(name, phone);
        if (opt.isEmpty()) {
            // 존재 여부 비노출 정책: 동일한 형태의 응답 반환
            return new FindIdResponse("*****@*****");
        }
        String masked = maskEmail(opt.get().getEmail());
        return new FindIdResponse(masked);
    }

    public void requestPasswordReset(PasswordResetRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            // 존재하지 않아도 동일 응답(열거 방지)
            return;
        }
        User user = opt.get();

        String cooldownKey = PR_COOLDOWN_PREFIX + email;
        if (RedisUtil.has(cooldownKey)) {
            // 존재하는 사용자에 대해서만 쿨다운을 적용(존재하지 않으면 조용히 성공)
            throw new ApiException(ErrorCode.PASSWORD_RESET_RESEND_COOLDOWN);
        }

        String code = UUID.randomUUID().toString();
        RedisUtil.setWithExpiryMin(PR_CODE_PREFIX + email, code, passwordResetTtlMinutes);
        RedisUtil.setWithExpiryMin(PR_TRIES_PREFIX + email, 0, passwordResetTtlMinutes);
        RedisUtil.setWithExpirySec(cooldownKey, "1", passwordResetResendCooldownSeconds);

        mailClient.sendPasswordResetEmail(email, user.getName(), passwordResetUrl, code, passwordResetTtlMinutes);
    }

    public void verifyPasswordReset(UserInfo userInfo, PasswordResetVerifyRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String codeKey = PR_CODE_PREFIX + email;
        String triesKey = PR_TRIES_PREFIX + email;

        String stored = RedisUtil.getStringValue(codeKey);
        if (stored == null) {
            throw new ApiException(ErrorCode.PASSWORD_RESET_CODE_EXPIRED);
        }
        Long tries = RedisUtil.getLongValue(triesKey);
        long currentTries = tries == null ? 0L : tries;
        if (currentTries >= passwordResetMaxRetry) {
            throw new ApiException(ErrorCode.PASSWORD_RESET_TOO_MANY_ATTEMPTS);
        }
        if (!stored.equals(request.getCode())) {
            RedisUtil.setWithExpiryMin(triesKey, currentTries + 1, passwordResetTtlMinutes);
            throw new ApiException(ErrorCode.PASSWORD_RESET_CODE_INVALID);
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        user.setPwd(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 무효화: 리프레시 토큰 삭제
        RedisUtil.delete(RT_KEY_PREFIX + user.getId());

        // 액세스 토큰 블랙리스트(Authorization 헤더가 제공된 경우)
        if (StringUtils.hasText(userInfo.getToken())) {
            try {
                Claims ac = jwtTokenProvider.parseClaims(userInfo.getToken());
                long ttlMs = ttlMsFromClaims(ac);
                if (ttlMs > 0) {
                    RedisUtil.setWithExpiryMs(BL_ACCESS_PREFIX + ac.getId(), "1", ttlMs);
                }
            } catch (Exception ignore) {
            }
        }

        // 사용된 코드 및 보조 키 삭제
        RedisUtil.delete(codeKey);
        RedisUtil.delete(triesKey);
        RedisUtil.delete(PR_COOLDOWN_PREFIX + email);
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return "*****@*****";
        }
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.substring(0, 2) + "****" + domain;
    }
}
