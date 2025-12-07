package seg.work.geuliumieum.server.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "SystemStatsResponse", description = "관리자 대시보드: 전체 누적 통계")
public class SystemStatsResponse {
    @Schema(description = "총 사용자 수")
    private long users;

    @Schema(description = "총 추모관 수")
    private long memorials;

    @Schema(description = "총 추모글 수")
    private long tributes;

    @Schema(description = "총 헌화/분향/헌촛 수")
    private long offerings;

    @Schema(description = "총 방명록 수")
    private long guestbooks;
}
