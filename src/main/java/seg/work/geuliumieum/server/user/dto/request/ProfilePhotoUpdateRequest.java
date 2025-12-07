package seg.work.geuliumieum.server.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "ProfilePhotoUpdateRequest", description = "프로필 사진 URL 업데이트 요청")
public class ProfilePhotoUpdateRequest {
    @NotBlank
    @Schema(description = "프로필 사진 URL", example = "https://.../bucket/key.jpg")
    private String profilePhotoUrl;
}
