package seg.work.geuliumieum.server.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "RefreshRequest", description = "Access 토큰 재발급 요청")
public class RefreshRequest {

    @Schema(description = "Refresh 토큰")
    private String refreshToken;
}
