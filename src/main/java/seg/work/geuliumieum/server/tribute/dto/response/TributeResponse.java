package seg.work.geuliumieum.server.tribute.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.common.entity.Tribute;

@Getter
@Builder
@Schema(name = "TributeResponse", description = "추모글 응답")
public class TributeResponse {
    @Schema(description = "추모글 ID")
    private Long id;
    @Schema(description = "추모관 ID")
    private Long memorialId;
    @Schema(description = "작성자 ID")
    private Long userId;
    @Schema(description = "내용")
    private String content;
    @Schema(description = "공개 여부")
    private Boolean isPublic;
    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;

    public static TributeResponse from(Tribute t) {
        return TributeResponse.builder()
            .id(t.getId())
            .memorialId(t.getMemorialId())
            .userId(t.getUserId())
            .content(t.getContent())
            .isPublic(t.getIsPublic())
            .createdAt(t.getCreatedAt())
            .updatedAt(t.getUpdatedAt())
            .build();
    }
}
