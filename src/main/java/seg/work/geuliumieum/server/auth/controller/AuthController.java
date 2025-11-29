package seg.work.geuliumieum.server.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.auth.dto.request.FindIdRequest;
import seg.work.geuliumieum.server.auth.dto.request.LoginRequest;
import seg.work.geuliumieum.server.auth.dto.request.LogoutRequest;
import seg.work.geuliumieum.server.auth.dto.request.PasswordResetRequest;
import seg.work.geuliumieum.server.auth.dto.request.PasswordResetVerifyRequest;
import seg.work.geuliumieum.server.auth.dto.request.RefreshRequest;
import seg.work.geuliumieum.server.auth.dto.request.RegisterRequest;
import seg.work.geuliumieum.server.auth.dto.request.ResendVerificationRequest;
import seg.work.geuliumieum.server.auth.dto.request.VerifyEmailRequest;
import seg.work.geuliumieum.server.auth.dto.response.FindIdResponse;
import seg.work.geuliumieum.server.auth.dto.response.MessageResponse;
import seg.work.geuliumieum.server.auth.dto.response.TokenResponse;
import seg.work.geuliumieum.server.auth.service.AuthService;
import seg.work.geuliumieum.server.common.dto.UserInfo;

/**
 * 인증/인가 관련 API를 제공하는 컨트롤러입니다. - 회원가입/이메일 인증/재발송/로그인/토큰 재발급/로그아웃/아이디 찾기/비밀번호 재설정 요청·검증을 처리합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "회원가입, 이메일 인증, 로그인/로그아웃, 토큰 재발급 등 인증 관련 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 요청을 처리합니다. 성공 시 201 Created와 메시지를 반환합니다.
     */
    @Operation(summary = "회원가입", description = "이메일/비밀번호 등 가입 정보를 제출하여 회원을 생성합니다.")
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        MessageResponse ok = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ok);
    }

    /**
     * 이메일 인증 코드를 검증하고 Access/Refresh 토큰을 발급합니다.
     */
    @Operation(summary = "이메일 인증", description = "메일로 전송된 인증 코드를 검증하고 최초 로그인용 토큰을 발급합니다.")
    @PostMapping("/verify-email")
    public ResponseEntity<TokenResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        TokenResponse tokens = authService.verifyEmail(request);
        return ResponseEntity.ok(tokens);
    }

    /**
     * 이메일 인증 코드를 재발송합니다. 성공 시 204 No Content를 반환합니다.
     */
    @Operation(summary = "이메일 인증 재발송", description = "인증 코드를 다시 발송합니다. 쿨다운/최대 시도 수 정책이 적용됩니다.")
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resend(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 이메일/비밀번호로 로그인하고 Access/Refresh 토큰을 발급합니다.
     */
    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하여 Access/Refresh 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return ResponseEntity.ok(tokens);
    }

    /**
     * Refresh 토큰(및 사용자 정보)을 기반으로 Access 토큰을 재발급합니다.
     */
    @Operation(summary = "토큰 재발급", description = "만료 임박/만료된 Access 토큰을 Refresh 토큰으로 재발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
        UserInfo userInfo,
        @RequestBody(required = false) RefreshRequest request) {
        TokenResponse tokens = authService.refresh(userInfo, request);
        return ResponseEntity.ok(tokens);
    }

    /**
     * 현재 사용자를 로그아웃 처리하고 액세스 토큰을 블랙리스트에 등록합니다.
     */
    @Operation(summary = "로그아웃", description = "현재 세션/토큰을 로그아웃 처리하고 액세스 토큰을 블랙리스트에 등록합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        UserInfo userInfo,
        @RequestBody(required = false) LogoutRequest request) {
        authService.logout(userInfo, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 가입된 이메일(ID) 찾기를 수행합니다.
     */
    @Operation(summary = "아이디 찾기", description = "이름/휴대전화 등 식별 정보를 이용해 가입된 이메일(ID)을 조회합니다.")
    @PostMapping("/find-id")
    public ResponseEntity<FindIdResponse> findId(@Valid @RequestBody FindIdRequest request) {
        FindIdResponse response = authService.findId(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 재설정 메일 발송을 요청합니다. 성공 시 204 No Content.
     */
    @Operation(summary = "비밀번호 재설정 요청", description = "비밀번호 재설정 메일 발송을 요청합니다.")
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 비밀번호 재설정 검증을 수행하고 비밀번호를 변경합니다.
     */
    @Operation(summary = "비밀번호 재설정 검증", description = "전달된 검증 코드 및 새 비밀번호로 재설정을 완료합니다.")
    @PostMapping("/password-reset/verify")
    public ResponseEntity<MessageResponse> verifyPasswordReset(
        UserInfo userInfo,
        @Valid @RequestBody PasswordResetVerifyRequest request) {
        authService.verifyPasswordReset(userInfo, request);
        return ResponseEntity.ok(new MessageResponse("비밀번호가 변경되었습니다"));
    }
}
