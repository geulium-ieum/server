package seg.work.geuliumieum.server.offering.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.common.entity.Offering;

@Getter
@Builder
@Schema(name = "OfferingResponse", description = "헌화/분향/헌촛 응답")
public class OfferingResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "추모관 ID")
    private Long memorialId;
    @Schema(description = "사용자 ID")
    private Long userId;
    @Schema(description = "유형")
    private String offeringType;
    @Schema(description = "메시지")
    private String message;

    public static OfferingResponse from(Offering o) {
        return OfferingResponse.builder()
            .id(o.getId())
            .memorialId(o.getMemorialId())
            .userId(o.getUserId())
            .offeringType(o.getOfferingType())
            .message(o.getMessage())
            .build();
    }
}
