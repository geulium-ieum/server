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

@Getter
@Setter
@Entity
@Table(name = "family_group_members")
public class FamilyGroupMember extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -4372165572577786407L;

    @NotNull
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ColumnDefault("'member'")
    @Column(name = "role", length = 20)
    private String role;

    @ColumnDefault("'active'")
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;

}