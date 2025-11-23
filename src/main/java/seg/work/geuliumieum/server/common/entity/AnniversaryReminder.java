package seg.work.geuliumieum.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "anniversary_reminders")
@Comment("기일 알림 테이블")
public class AnniversaryReminder extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -3745157951240266760L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "memorial_id", nullable = false)
    private Long memorialId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate;

    @Size(max = 20)
    @NotNull
    @Column(name = "reminder_type", nullable = false, length = 20)
    private String reminderType;

    @Size(max = 100)
    @Column(name = "custom_title", length = 100)
    private String customTitle;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @ColumnDefault("3")
    @Column(name = "notify_days_before")
    private Integer notifyDaysBefore;

}