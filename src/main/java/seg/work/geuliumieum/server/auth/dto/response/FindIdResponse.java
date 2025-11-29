package seg.work.geuliumieum.server.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "FindIdResponse", description = "마스킹된 이메일 응답")
public class FindIdResponse {

    @Schema(description = "마스킹된 이메일", example = "u***@example.com")
    private String maskedEmail;
}
