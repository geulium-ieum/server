package seg.work.geuliumieum.server.reminder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import seg.work.geuliumieum.server.reminder.constant.RepeatRule;

@Getter
@Setter
@Schema(name = "ReminderRequest", description = "기일 알림 생성 요청")
public class ReminderRequest {

    @NotBlank
    @Size(max = 200)
    @Schema(description = "제목", example = "아버지 기일")
    private String title;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "기준일(예: 기일)", example = "2025-01-15")
    private LocalDate reminderDate;

    @Schema(description = "반복 규칙", example = "YEARLY")
    private RepeatRule repeatRule = RepeatRule.YEARLY;

    @Schema(description = "며칠 전 알림", example = "3")
    private Integer daysBefore = 0;

    @Schema(description = "활성 여부", example = "true")
    private Boolean isActive = true;

    @Schema(description = "알림 채널", example = "INAPP")
    private String channel;
}
