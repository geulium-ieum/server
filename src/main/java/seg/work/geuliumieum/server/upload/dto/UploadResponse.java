package seg.work.geuliumieum.server.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "UploadResponse", description = "파일 업로드 응답")
public class UploadResponse {
    @Schema(description = "파일 식별자(삭제 시 사용)")
    private String fileId;

    @Schema(description = "접근 가능한 URL")
    private String url;

    @Schema(description = "바이트 크기")
    private long size;

    @Schema(description = "콘텐츠 타입")
    private String contentType;
}
