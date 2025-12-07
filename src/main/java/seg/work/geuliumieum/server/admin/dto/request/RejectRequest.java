package seg.work.geuliumieum.server.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "RejectRequest", description = "추모관 반려 사유 요청")
public class RejectRequest {
    @Schema(description = "반려 사유", example = "부적절한 내용 포함")
    private String reason;
}
