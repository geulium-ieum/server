package seg.work.geuliumieum.server.offering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OfferingStatsResponse", description = "헌화/분향/헌촛 통계 응답")
public class OfferingStatsResponse {

    @Schema(description = "총 개수")
    private long total;
    @Schema(description = "헌화 수")
    private long flower;
    @Schema(description = "분향 수")
    private long incense;
    @Schema(description = "헌촛 수")
    private long candle;
}
