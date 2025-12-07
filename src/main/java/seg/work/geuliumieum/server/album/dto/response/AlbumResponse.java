package seg.work.geuliumieum.server.album.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.common.entity.Album;

@Getter
@Builder
@Schema(name = "AlbumResponse", description = "앨범 응답")
public class AlbumResponse {

    @Schema(description = "앨범 ID")
    private Long id;
    @Schema(description = "추모관 ID")
    private Long memorialId;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "설명")
    private String description;
    @Schema(description = "생성자 ID")
    private Long createdBy;
    @Schema(description = "수정자 ID")
    private Long updatedBy;
    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;

    public static AlbumResponse from(Album a) {
        return AlbumResponse.builder()
            .id(a.getId())
            .memorialId(a.getMemorialId())
            .title(a.getTitle())
            .description(a.getDescription())
            .createdBy(a.getCreatedBy())
            .updatedBy(a.getUpdatedBy())
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
    }
}
