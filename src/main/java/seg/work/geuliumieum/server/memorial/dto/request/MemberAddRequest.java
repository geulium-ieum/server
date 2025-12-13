package seg.work.geuliumieum.server.memorial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "MemorialMemberAddRequest", description = "추모관 멤버 추가 요청")
public class MemberAddRequest {

    @NotNull(message = "{validation.memorial.member.userId.notNull}")
    @Schema(description = "추가할 사용자 ID", example = "123")
    private Long userId;

    @Schema(description = "역할(member|admin)", example = "member")
    private String role = "member";

    @Schema(description = "관계(선택)", example = "아들")
    private String relationship;
}
