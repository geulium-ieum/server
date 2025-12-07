package seg.work.geuliumieum.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import seg.work.geuliumieum.server.reminder.constant.RepeatRule;

@Getter
@Setter
@Entity
@Table(name = "reminders")
@Comment("기일 알림 테이블")
public class Reminder extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -7102234912345678901L;

    @NotNull
    @Column(name = "memorial_id", nullable = false)
    private Long memorialId;

    @NotNull
    @CreatedBy
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotNull
    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate; // 기준일(예: 기일)

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_rule", length = 20)
    private RepeatRule repeatRule = RepeatRule.YEARLY;

    @ColumnDefault("0")
    @Column(name = "days_before")
    private Integer daysBefore = 0; // 며칠 전 알림

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "channel", length = 20)
    private String channel; // EMAIL|INAPP 등

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
