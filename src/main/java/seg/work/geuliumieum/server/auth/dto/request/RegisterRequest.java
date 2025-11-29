package seg.work.geuliumieum.server.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "RegisterRequest", description = "회원가입 요청 본문")
public class RegisterRequest {

    @Email
    @NotBlank(message = "이메일은 필수값 입니다.")
    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @NotBlank(message = "비밀번호는 필수값 입니다.")
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+=-])(?=.*[0-9]).{8,20}$", message = "영문 숫자 특수기호 조합 8자리 이상 20자리 이하여야 합니다.")
    @Schema(description = "비밀번호(영문/숫자/특수문자 포함 8~20자)", example = "P@ssw0rd!")
    private String password;

    @NotBlank(message = "이름은 필수값 입니다.")
    @Size(max = 100)
    @Schema(description = "이름", example = "홍길동")
    private String name;

    @NotBlank(message = "전화번호는 필수값 입니다.")
    @Size(max = 20)
    @Pattern(regexp = "^(01[016789])-([0-9]{3,4})-([0-9]{4})$", message = "올바른 휴대폰 번호를 입력해주세요.")
    @Schema(description = "휴대전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "프로필 사진 URL", example = "https://cdn.example.com/profiles/abc.jpg")
    private String profilePhotoUrl;
}
