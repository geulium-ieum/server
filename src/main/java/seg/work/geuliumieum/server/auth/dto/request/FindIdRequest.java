package seg.work.geuliumieum.server.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindIdRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^(01[016789])-([0-9]{3,4})-([0-9]{4})$", message = "올바른 휴대폰 번호를 입력해주세요.")
    private String phone;
}
