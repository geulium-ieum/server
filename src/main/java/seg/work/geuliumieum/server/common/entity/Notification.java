package seg.work.geuliumieum.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;

@Getter
@Setter
@Entity
@Table(name = "notifications")
@Comment("알림 테이블")
public class Notification extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -5920524646439634667L;

    @NotNull
    @CreatedBy
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Size(max = 30)
    @NotNull
    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Size(max = 200)
    @NotNull
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotNull
    @Column(name = "message", nullable = false, length = Integer.MAX_VALUE)
    private String message;

    @Column(name = "related_id")
    private Long relatedId;

    @Size(max = 50)
    @Column(name = "related_type", length = 50)
    private String relatedType;

    @ColumnDefault("false")
    @Column(name = "is_read")
    private Boolean isRead;

}