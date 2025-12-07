package seg.work.geuliumieum.server.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "UserActivityResponse", description = "사용자 활동 통계 응답")
public class UserActivityResponse {

    @Schema(description = "추모글 수")
    private long tributeCount;

    @Schema(description = "헌화/분향/헌촛 수")
    private long offeringCount;

    @Schema(description = "방명록 수")
    private long guestbookCount;
}
