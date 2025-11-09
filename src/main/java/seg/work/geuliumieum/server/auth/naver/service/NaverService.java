package seg.work.geuliumieum.server.auth.naver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import seg.work.geuliumieum.server.auth.naver.dto.NaverLoginRequest;
import seg.work.geuliumieum.server.auth.naver.dto.NaverTokenResponse;
import seg.work.geuliumieum.server.auth.naver.dto.NaverUserInfo;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverService {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${auth.naver.client-id}")
    private String clientId;

    @Value("${auth.naver.client-secret}")
    private String clientSecret;

    private static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    public String getAccessToken(NaverLoginRequest request) {
        try {
            NaverTokenResponse response = webClient.post()
                .uri(NAVER_TOKEN_URL)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("redirect_uri", request.getRedirectUri())
                    .with("code", request.getCode()))
                .retrieve()
                .bodyToMono(NaverTokenResponse.class)
                .block();

            if (response != null && response.getAccessToken() != null) {
                return response.getAccessToken();
            } else {
                throw new ApiException(ErrorCode.GET_TOKEN_FROM_NAVER_FAILED);
            }
        } catch (Exception e) {
            throw new ApiException(ErrorCode.NAVER_ACCESS_TOKEN_ERROR);
        }
    }

    public NaverUserInfo getUserInfo(String accessToken) {
        try {
            return webClient.get()
                .uri(NAVER_USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(NaverUserInfo.class)
                .block();
        } catch (Exception e) {
            throw new ApiException(ErrorCode.NAVER_GET_USER_INFO);
        }
    }
}
