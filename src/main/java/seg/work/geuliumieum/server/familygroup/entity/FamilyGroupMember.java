package seg.work.geuliumieum.server.familygroup.entity;

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
import seg.work.geuliumieum.server.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "family_group_members")
@Comment("가족 그룹 멤버 테이블")
public class FamilyGroupMember extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -1712345678001122334L;

    @NotNull
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @NotNull
    @CreatedBy
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ColumnDefault("'member'")
    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;
}
