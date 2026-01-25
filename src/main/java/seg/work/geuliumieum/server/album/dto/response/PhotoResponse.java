package seg.work.geuliumieum.server.album.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seg.work.geuliumieum.server.common.entity.AlbumPhoto;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PhotoResponse", description = "앨범 사진 응답")
public class PhotoResponse {

    @Schema(description = "사진 ID")
    private Long id;
    @Schema(description = "앨범 ID")
    private Long albumId;
    @Schema(description = "사진 URL")
    private String photoUrl;
    @Schema(description = "캡션")
    private String caption;
    @Schema(description = "업로더 사용자 ID")
    private Long uploadedBy;

    public static PhotoResponse from(AlbumPhoto p) {
        return PhotoResponse.builder()
            .id(p.getId())
            .albumId(p.getAlbumId())
            .photoUrl(p.getPhotoUrl())
            .caption(p.getCaption())
            .uploadedBy(p.getUploadedBy())
            .build();
    }
}
