package seg.work.geuliumieum.server.familygroup.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "FamilyGroupMemberRoleUpdateRequest", description = "가족 그룹 멤버 역할 변경 요청")
public class MemberRoleUpdateRequest {

    @NotBlank(message = "{validation.memberRole.role.notBlank}")
    @Schema(description = "역할(member|admin)", example = "admin")
    private String role;
}
