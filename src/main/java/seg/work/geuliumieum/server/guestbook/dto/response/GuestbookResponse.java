package seg.work.geuliumieum.server.guestbook.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seg.work.geuliumieum.server.common.entity.GuestbookEntry;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "GuestbookResponse", description = "방명록 응답")
public class GuestbookResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "추모관 ID")
    private Long memorialId;
    @Schema(description = "작성자 사용자 ID(비회원일 수 있음)")
    private Long userId;
    @Schema(description = "작성자 이름")
    private String authorName;
    @Schema(description = "내용")
    private String content;
    @Schema(description = "승인 여부")
    private Boolean isApproved;

    public static GuestbookResponse from(GuestbookEntry e) {
        return GuestbookResponse.builder()
            .id(e.getId())
            .memorialId(e.getMemorialId())
            .userId(e.getUserId())
            .authorName(e.getAuthorName())
            .content(e.getContent())
            .isApproved(e.getIsApproved())
            .build();
    }
}
