package seg.work.geuliumieum.server.auth.naver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.auth.dto.TokenResponse;
import seg.work.geuliumieum.server.auth.naver.dto.NaverLoginRequest;
import seg.work.geuliumieum.server.auth.naver.service.NaverAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/naver")
public class NaverAuthController {

    private final NaverAuthService naverAuthService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody NaverLoginRequest request) {
        TokenResponse tokens = naverAuthService.login(request);
        return ResponseEntity.ok(tokens);
    }
}
