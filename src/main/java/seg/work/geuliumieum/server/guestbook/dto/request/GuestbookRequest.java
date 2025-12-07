package seg.work.geuliumieum.server.guestbook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "GuestbookRequest", description = "방명록 작성/수정 요청")
public class GuestbookRequest {

    @NotBlank
    @Schema(description = "작성자 이름", example = "홍길동")
    private String authorName;

    @NotBlank
    @Schema(description = "내용", example = "깊은 애도를 표합니다.")
    private String content;
}
