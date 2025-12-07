package seg.work.geuliumieum.server.familygroup.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.familygroup.entity.FamilyGroupMember;

@Getter
@Builder
@Schema(name = "FamilyGroupMemberResponse", description = "가족 그룹 멤버 응답")
public class FamilyGroupMemberResponse {

    @Schema(description = "멤버 레코드 ID")
    private Long id;
    @Schema(description = "그룹 ID")
    private Long groupId;
    @Schema(description = "사용자 ID")
    private Long userId;
    @Schema(description = "역할(member|admin)")
    private String role;
    @Schema(description = "가입 시각")
    private OffsetDateTime joinedAt;

    public static FamilyGroupMemberResponse from(FamilyGroupMember m) {
        return FamilyGroupMemberResponse.builder()
            .id(m.getId())
            .groupId(m.getGroupId())
            .userId(m.getUserId())
            .role(m.getRole())
            .joinedAt(m.getJoinedAt())
            .build();
    }
}
