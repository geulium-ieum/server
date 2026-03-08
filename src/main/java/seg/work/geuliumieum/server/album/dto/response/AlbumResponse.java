package seg.work.geuliumieum.server.album.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seg.work.geuliumieum.server.common.entity.Album;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AlbumResponse", description = "앨범 응답")
public class AlbumResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "앨범 ID")
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "추모관 ID")
    private Long memorialId;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "설명")
    private String description;
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "생성자 ID")
    private Long createdBy;
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "수정자 ID")
    private Long updatedBy;
    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;

    public static AlbumResponse from(Album album) {
        return AlbumResponse.builder()
            .id(album.getId())
            .memorialId(album.getMemorialId())
            .title(album.getTitle())
            .description(album.getDescription())
            .createdBy(album.getCreatedBy())
            .updatedBy(album.getUpdatedBy())
            .createdAt(album.getCreatedAt())
            .updatedAt(album.getUpdatedAt())
            .build();
    }
}
