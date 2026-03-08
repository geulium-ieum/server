package seg.work.geuliumieum.server.reminder.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.common.entity.Reminder;
import seg.work.geuliumieum.server.reminder.constant.RepeatRule;

@Getter
@Builder
@Schema(name = "ReminderResponse", description = "기일 알림 응답")
public class ReminderResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "ID")
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "추모관 ID")
    private Long memorialId;
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "사용자 ID")
    private Long userId;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "기준일")
    private LocalDate reminderDate;
    @Schema(description = "반복 규칙")
    private RepeatRule repeatRule;
    @Schema(description = "며칠 전")
    private Integer daysBefore;
    @Schema(description = "활성 여부")
    private Boolean isActive;
    @Schema(description = "채널")
    private String channel;
    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;

    @Schema(description = "다음 알림 발생일(기준일-사전일수)")
    private LocalDate nextOccurrence;

    public static ReminderResponse from(Reminder reminder) {
        return ReminderResponse.builder()
            .id(reminder.getId())
            .memorialId(reminder.getMemorialId())
            .userId(reminder.getUserId())
            .title(reminder.getTitle())
            .reminderDate(reminder.getReminderDate())
            .repeatRule(reminder.getRepeatRule())
            .daysBefore(reminder.getDaysBefore())
            .isActive(reminder.getIsActive())
            .channel(reminder.getChannel())
            .createdAt(reminder.getCreatedAt())
            .updatedAt(reminder.getUpdatedAt())
            .build();
    }
}
