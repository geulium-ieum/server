package seg.work.geuliumieum.server.tribute.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TributeRequest", description = "추모글 작성/수정 요청")
public class TributeRequest {
    @NotBlank
    @Schema(description = "내용", example = "그리운 어머니, 항상 사랑합니다.")
    private String content;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic = true;
}
