package seg.work.geuliumieum.server.stats.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "StatsOverviewResponse", description = "시스템 전체 공개 통계")
public class StatsOverviewResponse {
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
