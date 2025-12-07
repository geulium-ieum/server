package seg.work.geuliumieum.server.familygroup.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "FamilyGroupUpdateRequest", description = "가족 그룹 수정 요청")
public class FamilyGroupUpdateRequest {

    @Size(max = 100)
    @Schema(description = "그룹명", example = "홍씨 일가")
    private String name;

    @Size(max = 1000)
    @Schema(description = "설명")
    private String description;
}
