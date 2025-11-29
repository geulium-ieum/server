package seg.work.geuliumieum.server.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "TokenResponse", description = "발급된 토큰 정보 응답")
public class TokenResponse {

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;

    @Schema(description = "Access 토큰")
    private String accessToken;

    @Schema(description = "Access 토큰 만료까지 남은 시간(초)", example = "21600")
    private long accessTokenExpiresIn;

    @Schema(description = "Refresh 토큰")
    private String refreshToken;

    @Schema(description = "Refresh 토큰 만료까지 남은 시간(초)", example = "7776000")
    private long refreshTokenExpiresIn;
}
