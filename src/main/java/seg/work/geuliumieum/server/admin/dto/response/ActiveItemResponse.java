package seg.work.geuliumieum.server.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "ActiveItemResponse", description = "활동 상위 항목 응답 (ID/건수)")
public class ActiveItemResponse {
    @Schema(description = "리소스 ID")
    private Long id;

    @Schema(description = "활동 건수")
    private long count;
}
