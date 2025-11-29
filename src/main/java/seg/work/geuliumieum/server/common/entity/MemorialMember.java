package seg.work.geuliumieum.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;

@Getter
@Setter
@Entity
@Table(name = "memorial_members")
@Comment("추모관 멤버 (가족 구성원) 테이블")
public class MemorialMember extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -3940251787841327815L;

    @NotNull
    @Column(name = "memorial_id", nullable = false)
    private Long memorialId;

    @NotNull
    @CreatedBy
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "relationship", length = 50)
    private String relationship;

    @ColumnDefault("'member'")
    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;

}