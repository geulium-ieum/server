package seg.work.geuliumieum.server.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UUID;

@Getter
@Setter
public class PasswordResetVerifyRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @UUID(message = "유효한 코드 형식이 아닙니다")
    private String code;

    @NotBlank
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+=-])(?=.*[0-9]).{8,20}$", message = "영문 숫자 특수기호 조합 8자리 이상 20자리 이하여야 합니다.")
    private String newPassword;
}
