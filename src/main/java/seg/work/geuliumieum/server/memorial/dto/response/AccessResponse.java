package seg.work.geuliumieum.server.memorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MemorialAccessResponse", description = "추모관 접근 권한 응답")
public class AccessResponse {
    @Schema(description = "접근 가능 여부")
    private boolean allowed;

    @Schema(description = "사유")
    private String reason;
}
