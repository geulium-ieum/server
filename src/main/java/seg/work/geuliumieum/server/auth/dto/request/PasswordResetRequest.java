package seg.work.geuliumieum.server.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "PasswordResetRequest", description = "비밀번호 재설정 메일 발송 요청")
public class PasswordResetRequest {

    @Email
    @NotBlank
    @Schema(description = "이메일", example = "user@example.com")
    private String email;
}
