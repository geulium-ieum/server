package seg.work.geuliumieum.server.album.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "AlbumUpdateRequest", description = "앨범 수정 요청")
public class AlbumUpdateRequest {

    @Size(max = 200)
    @Schema(description = "앨범 제목", example = "가족 사진 모음")
    private String title;

    @Size(max = 3000)
    @Schema(description = "설명")
    private String description;
}
