package seg.work.geuliumieum.server.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.auth.dto.request.FindIdRequest;
import seg.work.geuliumieum.server.auth.dto.response.FindIdResponse;
import seg.work.geuliumieum.server.auth.dto.request.LoginRequest;
import seg.work.geuliumieum.server.auth.dto.request.LogoutRequest;
import seg.work.geuliumieum.server.auth.dto.response.MessageResponse;
import seg.work.geuliumieum.server.auth.dto.request.PasswordResetRequest;
import seg.work.geuliumieum.server.auth.dto.request.PasswordResetVerifyRequest;
import seg.work.geuliumieum.server.auth.dto.request.RefreshRequest;
import seg.work.geuliumieum.server.auth.dto.request.RegisterRequest;
import seg.work.geuliumieum.server.auth.dto.request.ResendVerificationRequest;
import seg.work.geuliumieum.server.auth.dto.response.TokenResponse;
import seg.work.geuliumieum.server.auth.dto.request.VerifyEmailRequest;
import seg.work.geuliumieum.server.auth.service.AuthService;
import seg.work.geuliumieum.server.common.dto.UserInfo;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        MessageResponse ok = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ok);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<TokenResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        TokenResponse tokens = authService.verifyEmail(request);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resend(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(UserInfo userInfo, @RequestBody(required = false) RefreshRequest request) {
        TokenResponse tokens = authService.refresh(userInfo, request);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(UserInfo userInfo, @RequestBody(required = false) LogoutRequest request) {
        authService.logout(userInfo, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/find-id")
    public ResponseEntity<FindIdResponse> findId(@Valid @RequestBody FindIdRequest request) {
        FindIdResponse response = authService.findId(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/password-reset/verify")
    public ResponseEntity<MessageResponse> verifyPasswordReset(UserInfo userInfo, @Valid @RequestBody PasswordResetVerifyRequest request) {
        authService.verifyPasswordReset(userInfo, request);
        return ResponseEntity.ok(new MessageResponse("비밀번호가 변경되었습니다"));
    }
}
