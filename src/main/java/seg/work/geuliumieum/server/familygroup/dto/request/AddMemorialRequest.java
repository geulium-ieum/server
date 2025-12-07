package seg.work.geuliumieum.server.familygroup.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "FamilyGroupAddMemorialRequest", description = "가족 그룹에 추모관 추가 요청")
public class AddMemorialRequest {

    @NotNull(message = "{validation.addMemorial.memorialId.notNull}")
    @Schema(description = "추가할 추모관 ID", example = "1001")
    private Long memorialId;
}
