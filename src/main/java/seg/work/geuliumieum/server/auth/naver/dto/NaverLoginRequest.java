package seg.work.geuliumieum.server.auth.naver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NaverLoginRequest {

    @NotBlank(message = "인가 코드는 필수값 입니다.")
    private String code;

    @NotBlank(message = "리다이렉트 URL은 필수값 입니다.")
    private String redirectUri;
}
