package seg.work.geuliumieum.server.auth.kakao.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.auth.dto.response.TokenResponse;
import seg.work.geuliumieum.server.auth.kakao.dto.KakaoLoginRequest;
import seg.work.geuliumieum.server.auth.kakao.service.KakaoAuthService;

/**
 * 카카오 소셜 로그인 API.
 * - 카카오 인가코드/토큰 정보를 받아 로그인 처리 후 애플리케이션 토큰을 발급합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/kakao")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    /** 카카오 계정으로 로그인하고 Access/Refresh 토큰을 발급합니다. */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody KakaoLoginRequest request) {
        TokenResponse tokens = kakaoAuthService.login(request);
        return ResponseEntity.ok(tokens);
    }
}
