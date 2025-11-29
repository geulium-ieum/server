package seg.work.geuliumieum.server.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "FindIdRequest", description = "아이디(이메일) 찾기 요청")
public class FindIdRequest {

    @NotBlank
    @Schema(description = "이름", example = "홍길동")
    private String name;

    @NotBlank
    @Pattern(regexp = "^(01[016789])-([0-9]{3,4})-([0-9]{4})$", message = "올바른 휴대폰 번호를 입력해주세요.")
    @Schema(description = "휴대전화번호", example = "010-1234-5678")
    private String phone;
}
