package seg.work.geuliumieum.server.stats.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "MemorialStatsResponse", description = "특정 추모관 공개 통계")
public class MemorialStatsResponse {

    @Schema(description = "추모글 수")
    private long tributeCount;

    @Schema(description = "헌화/분향/헌촛 수")
    private long offeringCount;

    @Schema(description = "방명록 수")
    private long guestbookCount;

    @Schema(description = "앨범 수")
    private long albumCount;

    @Schema(description = "사진 수")
    private long photoCount;
}
