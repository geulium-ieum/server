package seg.work.geuliumieum.server.announcement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "AnnouncementCreateRequest", description = "공지사항 생성 요청")
public class AnnouncementCreateRequest {

    @NotBlank
    @Size(max = 200)
    @Schema(description = "제목", example = "서비스 공지")
    private String title;

    @NotBlank
    @Schema(description = "내용")
    private String content;

    @Schema(description = "상단 고정 여부", example = "false")
    private Boolean isPinned = false;
}
