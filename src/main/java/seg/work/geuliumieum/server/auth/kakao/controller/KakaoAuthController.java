package seg.work.geuliumieum.server.auth.kakao.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.auth.dto.TokenResponse;
import seg.work.geuliumieum.server.auth.kakao.dto.KakaoLoginRequest;
import seg.work.geuliumieum.server.auth.kakao.service.KakaoAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/kakao")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody KakaoLoginRequest request) {
        TokenResponse tokens = kakaoAuthService.login(request);
        return ResponseEntity.ok(tokens);
    }
}
