package seg.work.geuliumieum.server.tribute.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seg.work.geuliumieum.server.common.entity.Tribute;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TributeResponse", description = "추모글 응답")
public class TributeResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "추모글 ID")
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "추모관 ID")
    private Long memorialId;
    @JsonSerialize(using = ToStringSerializer.class)
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

    public static TributeResponse from(Tribute tribute) {
        return TributeResponse.builder()
            .id(tribute.getId())
            .memorialId(tribute.getMemorialId())
            .userId(tribute.getUserId())
            .content(tribute.getContent())
            .isPublic(tribute.getIsPublic())
            .createdAt(tribute.getCreatedAt())
            .updatedAt(tribute.getUpdatedAt())
            .build();
    }
}
