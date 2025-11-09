package seg.work.geuliumieum.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @Email
    @NotBlank(message = "이메일은 필수값 입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수값 입니다.")
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+=-])(?=.*[0-9]).{8,20}$", message = "영문 숫자 특수기호 조합 8자리 이상 20자리 이하여야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수값 입니다.")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "전화번호는 필수값 입니다.")
    @Size(max = 20)
    @Pattern(regexp = "^(01[016789])-([0-9]{3,4})-([0-9]{4})$", message = "올바른 휴대폰 번호를 입력해주세요.")
    private String phone;

    private String profilePhotoUrl;
}
