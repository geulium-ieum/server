package seg.work.geuliumieum.server.familygroup.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seg.work.geuliumieum.server.common.entity.FamilyGroup;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "FamilyGroupResponse", description = "가족 그룹 응답")
public class FamilyGroupResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "그룹 ID")
    private Long id;
    @Schema(description = "그룹명")
    private String name;
    @Schema(description = "설명")
    private String description;
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "소유자 사용자 ID")
    private Long ownerId;
    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;

    public static FamilyGroupResponse from(FamilyGroup e) {
        return FamilyGroupResponse.builder()
            .id(e.getId())
            .name(e.getName())
            .description(e.getDescription())
            .ownerId(e.getOwnerId())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }
}
