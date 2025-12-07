package seg.work.geuliumieum.server.album.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "PhotoUpdateRequest", description = "앨범 사진 캡션 수정 요청")
public class PhotoUpdateRequest {

    @Size(max = 3000)
    @Schema(description = "캡션")
    private String caption;
}
