package seg.work.geuliumieum.server.offering.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OfferingRequest", description = "헌화/분향/헌촛 생성 요청")
public class OfferingRequest {

    @NotBlank
    @Schema(description = "헌화/분향/헌촛 유형", example = "FLOWER")
    private String offeringType;

    @Schema(description = "메시지", example = "평안히 쉬세요")
    private String message;
}
