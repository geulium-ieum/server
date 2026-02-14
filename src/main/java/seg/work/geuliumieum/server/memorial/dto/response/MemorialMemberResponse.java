package seg.work.geuliumieum.server.memorial.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.common.entity.MemorialMember;

@Getter
@Builder
@Schema(name = "MemorialMemberResponse", description = "추모관 멤버 조회 응답")
public class MemorialMemberResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "멤버 레코드 ID")
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "추모관 ID")
    private Long memorialId;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "관계")
    private String relationship;

    @Schema(description = "역할(member|admin)")
    private String role;

    @Schema(description = "가입 시각")
    private OffsetDateTime joinedAt;

    public static MemorialMemberResponse from(MemorialMember e) {
        return MemorialMemberResponse.builder()
            .id(e.getId())
            .memorialId(e.getMemorialId())
            .userId(e.getUserId())
            .relationship(e.getRelationship())
            .role(e.getRole())
            .joinedAt(e.getJoinedAt())
            .build();
    }
}
