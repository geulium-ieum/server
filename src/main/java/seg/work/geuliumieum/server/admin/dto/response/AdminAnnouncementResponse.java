package seg.work.geuliumieum.server.admin.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.common.entity.Announcement;

@Getter
@Builder
@Schema(name = "AdminAnnouncementResponse", description = "공지사항 응답")
public class AdminAnnouncementResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "내용")
    private String content;
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "작성자 ID")
    private Long authorId;
    @Schema(description = "상단 고정 여부")
    private Boolean isPinned;
    @Schema(description = "발행 여부")
    private Boolean isPublished;
    @Schema(description = "발행 시각")
    private OffsetDateTime publishedAt;
    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;

    public static AdminAnnouncementResponse from(Announcement announcement) {
        return AdminAnnouncementResponse.builder()
            .id(announcement.getId())
            .title(announcement.getTitle())
            .content(announcement.getContent())
            .authorId(announcement.getAuthorId())
            .isPinned(announcement.getIsPinned())
            .isPublished(announcement.getIsPublished())
            .publishedAt(announcement.getPublishedAt())
            .createdAt(announcement.getCreatedAt())
            .updatedAt(announcement.getUpdatedAt())
            .build();
    }
}
