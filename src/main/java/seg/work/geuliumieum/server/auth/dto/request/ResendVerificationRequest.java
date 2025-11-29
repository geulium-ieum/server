package seg.work.geuliumieum.server.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "ResendVerificationRequest", description = "이메일 인증 코드 재발송 요청")
public class ResendVerificationRequest {

    @Email
    @NotBlank
    @Schema(description = "이메일", example = "user@example.com")
    private String email;
}
