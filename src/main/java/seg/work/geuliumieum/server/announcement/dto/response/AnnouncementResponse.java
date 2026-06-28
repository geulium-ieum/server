package seg.work.geuliumieum.server.announcement.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seg.work.geuliumieum.server.common.entity.Announcement;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AnnouncementResponse", description = "공지사항 응답")
public class AnnouncementResponse {

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

    @Schema(description = "작성자 이름")
    private String authorName;

    public AnnouncementResponse(Announcement announcement, String authorName) {
        this.id = announcement.getId();
        this.title = announcement.getTitle();
        this.content = announcement.getContent();
        this.authorId = announcement.getAuthorId();
        this.isPinned = announcement.getIsPinned();
        this.isPublished = announcement.getIsPublished();
        this.publishedAt = announcement.getPublishedAt();
        this.createdAt = announcement.getCreatedAt();
        this.updatedAt = announcement.getUpdatedAt();

        this.authorName = authorName;
    }
}