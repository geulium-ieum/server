package seg.work.geuliumieum.server.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "LogoutRequest", description = "로그아웃 요청(선택적으로 Refresh 토큰 포함)")
public class LogoutRequest {

    @Schema(description = "Refresh 토큰(선택)")
    private String refreshToken;
}
