package seg.work.geuliumieum.server.auth.kakao.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.auth.dto.response.TokenResponse;
import seg.work.geuliumieum.server.auth.kakao.dto.KakaoLoginRequest;
import seg.work.geuliumieum.server.auth.kakao.dto.KakaoUserInfo;
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
public class KakaoAuthService {

    private final KakaoService kakaoService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private static final String RT_KEY_PREFIX = "rt:user:";

    @Transactional
    public TokenResponse login(KakaoLoginRequest request) {
        KakaoUserInfo kakaoUser = getKakaoUserInfo(request);
        if (kakaoUser == null) {
            throw new ApiException(ErrorCode.KAKAO_GET_USER_INFO);
        }

        String email = extractEmail(kakaoUser);
        String nickName = extractNickName(kakaoUser);
        String profileImage = extractProfileImage(kakaoUser);

        // 이메일은 소셜에서 종종 대문자 등으로 올 수 있으므로 정규화
        if (email != null) {
            email = email.trim().toLowerCase();
        }

        Optional<User> optUser = email == null ? Optional.empty() : userRepository.findByEmailAndDeletedAtIsNull(email);
        User user = optUser.orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setPwd(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            user.setName(nickName != null ? nickName : "카카오사용자");
            user.setProfilePhotoUrl(profileImage);
            user.setProvider("KAKAO");
            user.setProvider(kakaoUser.getId().toString());
            user.setRole(UserRole.USER);
            user.setIsActive(true);

            user = userRepository.save(user);
        }

        // 기존 사용자라면 프로필 최신화(선택)
        if (nickName != null && !nickName.equals(user.getName())) {
            user.setName(nickName);
        }
        if (profileImage != null && (user.getProfilePhotoUrl() == null || !profileImage.equals(user.getProfilePhotoUrl()))) {
            user.setProfilePhotoUrl(profileImage);
        }
        if (user.getIsActive() == null || !user.getIsActive()) {
            user.setIsActive(true);
        }
        user.setLastLoginAt(OffsetDateTime.now());
        user = userRepository.save(user);

        java.util.Map<String, Object> claims = defaultClaims(user);
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), claims);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), claims);

        RedisUtil.setWithExpiryMs(
            RT_KEY_PREFIX + user.getId(),
            refreshToken,
            JwtTokenProvider.REFRESH_TOKEN_TTL_MILLIS
        );

        return TokenResponse.builder()
            .tokenType("Bearer")
            .accessToken(accessToken)
            .accessTokenExpiresIn(JwtTokenProvider.ACCESS_TOKEN_TTL_MILLIS)
            .refreshToken(refreshToken)
            .refreshTokenExpiresIn(JwtTokenProvider.REFRESH_TOKEN_TTL_MILLIS)
            .build();
    }

    private KakaoUserInfo getKakaoUserInfo(KakaoLoginRequest request) {
        // 1. 카카오로부터 액세스 토큰 획득
        String accessToken = kakaoService.getAccessToken(request);

        // 2. 액세스 토큰으로 사용자 정보 조회
        return kakaoService.getUserInfo(accessToken);
    }

    private String extractEmail(KakaoUserInfo info) {
        if (info.getKakaoAccount() != null && Boolean.TRUE.equals(info.getKakaoAccount().getHasEmail())) {
            String email = info.getKakaoAccount().getEmail();
            if (Boolean.TRUE.equals(info.getKakaoAccount().getIsEmailValid()) && Boolean.TRUE.equals(info.getKakaoAccount().getIsEmailVerified())) {
                return email;
            }
            return email; // 유효성/검증 플래그가 없어도 이메일이 있으면 사용
        }
        return null;
    }

    private String extractNickName(KakaoUserInfo info) {
        if (info.getKakaoAccount() != null && info.getKakaoAccount().getProfile() != null) {
            if (info.getKakaoAccount().getProfile().getNickname() != null) {
                return info.getKakaoAccount().getProfile().getNickname();
            }
        }
        if (info.getProperties() != null && info.getProperties().getNickname() != null) {
            return info.getProperties().getNickname();
        }
        return null;
    }

    private String extractProfileImage(KakaoUserInfo info) {
        if (info.getKakaoAccount() != null && info.getKakaoAccount().getProfile() != null) {
            if (info.getKakaoAccount().getProfile().getProfileImageUrl() != null) {
                return info.getKakaoAccount().getProfile().getProfileImageUrl();
            }
            if (info.getKakaoAccount().getProfile().getThumbnailImageUrl() != null) {
                return info.getKakaoAccount().getProfile().getThumbnailImageUrl();
            }
        }
        if (info.getProperties() != null) {
            if (info.getProperties().getProfileImage() != null) {
                return info.getProperties().getProfileImage();
            }
            if (info.getProperties().getThumbnailImage() != null) {
                return info.getProperties().getThumbnailImage();
            }
        }
        return null;
    }

    private java.util.Map<String, Object> defaultClaims(User user) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("uid", user.getId());
        claims.put("role", user.getRole() == null ? "USER" : user.getRole().name());
        claims.put("name", user.getName());
        return claims;
    }
}