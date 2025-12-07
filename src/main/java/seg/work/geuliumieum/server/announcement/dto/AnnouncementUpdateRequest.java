package seg.work.geuliumieum.server.announcement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "AnnouncementUpdateRequest", description = "공지사항 수정 요청")
public class AnnouncementUpdateRequest {

    @Size(max = 200)
    @Schema(description = "제목")
    private String title;

    @Schema(description = "내용")
    private String content;

    @Schema(description = "상단 고정 여부")
    private Boolean isPinned;
}
