package seg.work.geuliumieum.server.auth.kakao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import seg.work.geuliumieum.server.auth.kakao.dto.KakaoLoginRequest;
import seg.work.geuliumieum.server.auth.kakao.dto.KakaoTokenResponse;
import seg.work.geuliumieum.server.auth.kakao.dto.KakaoUserInfo;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${auth.kakao.client-id}")
    private String clientId;

    @Value("${auth.kakao.client-secret}")
    private String clientSecret;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    public String getAccessToken(KakaoLoginRequest request) {
        try {
            KakaoTokenResponse response = webClient.post()
                .uri(KAKAO_TOKEN_URL)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("redirect_uri", request.getRedirectUri())
                    .with("code", request.getCode()))
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();

            if (response != null) {
                return response.getAccessToken();
            } else {
                throw new ApiException(ErrorCode.GET_TOKEN_FROM_KAKAO_FAILED);
            }
        } catch (Exception e) {
            log.error("Kakao access token error", e);
            throw new ApiException(ErrorCode.KAKAO_ACCESS_TOKEN_ERROR);
        }
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            return webClient.get()
                .uri(KAKAO_USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfo.class)
                .block();
        } catch (Exception e) {
            throw new ApiException(ErrorCode.KAKAO_GET_USER_INFO);
        }
    }
}