package seg.work.geuliumieum.server.album.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "PhotoCreateRequest", description = "앨범 사진 업로드(URL 등록) 요청")
public class PhotoCreateRequest {

    @NotBlank(message = "{validation.photo.url.notBlank}")
    @Schema(description = "사진 URL (S3 등)", example = "https://.../bucket/key.jpg")
    private String photoUrl;

    @Schema(description = "캡션")
    private String caption;
}
