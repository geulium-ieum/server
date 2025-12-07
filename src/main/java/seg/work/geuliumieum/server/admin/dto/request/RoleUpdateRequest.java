package seg.work.geuliumieum.server.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import seg.work.geuliumieum.server.config.security.UserRole;

@Getter
@Setter
@Schema(name = "RoleUpdateRequest", description = "관리자: 사용자 역할 변경 요청")
public class RoleUpdateRequest {

    @NotNull(message = "{validation.admin.role.notNull}")
    @Schema(description = "변경할 역할", example = "ADMIN")
    private UserRole role;
}
