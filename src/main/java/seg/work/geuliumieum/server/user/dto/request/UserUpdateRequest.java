package seg.work.geuliumieum.server.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "UserUpdateRequest", description = "사용자 정보 수정 요청")
public class UserUpdateRequest {

    @Size(max = 100)
    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Size(max = 20)
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;
}
