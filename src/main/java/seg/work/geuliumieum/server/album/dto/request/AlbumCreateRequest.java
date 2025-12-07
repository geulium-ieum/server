package seg.work.geuliumieum.server.album.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "AlbumCreateRequest", description = "앨범 생성 요청")
public class AlbumCreateRequest {

    @NotBlank(message = "{validation.album.title.notBlank}")
    @Size(max = 200)
    @Schema(description = "앨범 제목", example = "가족 사진 모음")
    private String title;

    @Size(max = 3000)
    @Schema(description = "설명")
    private String description;
}
