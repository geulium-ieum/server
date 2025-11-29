package seg.work.geuliumieum.server.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "MessageResponse", description = "간단한 메시지 응답")
public class MessageResponse {

    @Schema(description = "메시지")
    private String message;
}
