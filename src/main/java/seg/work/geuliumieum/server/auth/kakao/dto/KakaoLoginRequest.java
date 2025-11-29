package seg.work.geuliumieum.server.auth.kakao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "KakaoLoginRequest", description = "카카오 로그인 요청")
public class KakaoLoginRequest {

    @NotBlank(message = "인가 코드는 필수값 입니다.")
    @Schema(description = "카카오 인가 코드")
    private String code;

    @NotBlank(message = "리다이렉트 URL은 필수값 입니다.")
    @Schema(description = "인가 코드 발급에 사용된 리다이렉트 URI")
    private String redirectUri;
}