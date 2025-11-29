package seg.work.geuliumieum.server.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UUID;

@Getter
@Setter
@Schema(name = "VerifyEmailRequest", description = "이메일 인증 코드 검증 요청")
public class VerifyEmailRequest {

    @Email
    @NotBlank
    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @NotBlank
    @UUID(message = "유효한 코드 형식이 아닙니다")
    @Schema(description = "인증 코드(UUID)")
    private String code;
}
