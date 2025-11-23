package seg.work.geuliumieum.server.auth.naver.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.auth.dto.response.TokenResponse;
import seg.work.geuliumieum.server.auth.naver.dto.NaverLoginRequest;
import seg.work.geuliumieum.server.auth.naver.dto.NaverUserInfo;
import seg.work.geuliumieum.server.common.entity.User;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.UserRepository;
import seg.work.geuliumieum.server.config.security.UserRole;
import seg.work.geuliumieum.server.config.security.jwt.JwtTokenProvider;
import seg.work.geuliumieum.server.util.RedisUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverAuthService {

    private final NaverService naverService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private static final String RT_KEY_PREFIX = "rt:user:";

    @Transactional
    public TokenResponse login(NaverLoginRequest request) {
        NaverUserInfo userInfo = getNaverUserInfo(request);
        if (userInfo == null || userInfo.getResponse() == null) {
            throw new ApiException(ErrorCode.NAVER_GET_USER_INFO);
        }

        String naverId = userInfo.getResponse().getId();
        String email = userInfo.getResponse().getEmail();
        String name = userInfo.getResponse().getName();
        if (name == null || name.isBlank()) {
            name = userInfo.getResponse().getNickname();
        }
        String profileImage = userInfo.getResponse().getProfileImage();

        if (email != null) {
            email = email.trim().toLowerCase();
        }

        Optional<User> opt = email == null ? Optional.empty() : userRepository.findByEmail(email);
        User user = opt.orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setPwd(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setName(name != null ? name : "네이버사용자");
            user.setProfilePhotoUrl(profileImage);
            user.setRole(UserRole.USER);
            user.setIsActive(true);
            user.setProvider("NAVER");
            user.setProviderId(naverId);

            user = userRepository.save(user);
        }

        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
        }
        if (profileImage != null && (user.getProfilePhotoUrl() == null || !profileImage.equals(user.getProfilePhotoUrl()))) {
            user.setProfilePhotoUrl(profileImage);
        }
        if (user.getIsActive() == null || !user.getIsActive()) {
            user.setIsActive(true);
        }
        if (user.getProvider() == null) {
            user.setProvider("NAVER");
        }
        if (user.getProviderId() == null) {
            user.setProviderId(naverId);
        }
        user.setLastLoginAt(java.time.OffsetDateTime.now());
        user = userRepository.save(user);

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

    private NaverUserInfo getNaverUserInfo(NaverLoginRequest request) {
        String accessToken = naverService.getAccessToken(request);
        return naverService.getUserInfo(accessToken);
    }

    private Map<String, Object> defaultClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", user.getId());
        claims.put("role", user.getRole() == null ? "USER" : user.getRole().name());
        claims.put("name", user.getName());
        return claims;
    }
}
