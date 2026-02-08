package seg.work.geuliumieum.server.notification.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.common.entity.Notification;

@Getter
@Builder
@Schema(name = "NotificationResponse", description = "알림 응답")
public class NotificationResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "알림 ID")
    private Long id;
    @Schema(description = "사용자 ID")
    private Long userId;
    @Schema(description = "알림 유형")
    private String type;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "메시지")
    private String message;
    @Schema(description = "관련 리소스 타입")
    private String relatedType;
    @Schema(description = "관련 리소스 ID")
    private Long relatedId;
    @Schema(description = "읽음 여부")
    private Boolean isRead;
    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
            .id(n.getId())
            .userId(n.getUserId())
            .type(n.getType())
            .title(n.getTitle())
            .message(n.getMessage())
            .relatedType(n.getRelatedType())
            .relatedId(n.getRelatedId())
            .isRead(n.getIsRead())
            .createdAt(n.getCreatedAt())
            .build();
    }
}
