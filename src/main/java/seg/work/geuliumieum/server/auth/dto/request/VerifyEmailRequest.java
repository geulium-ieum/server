package seg.work.geuliumieum.server.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UUID;

@Getter
@Setter
public class VerifyEmailRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @UUID(message = "유효한 코드 형식이 아닙니다")
    private String code;
}
