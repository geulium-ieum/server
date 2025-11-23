package seg.work.geuliumieum.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "family_group_memorials")
public class FamilyGroupMemorial extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -4898587917095768204L;

    @NotNull
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @NotNull
    @Column(name = "memorial_id", nullable = false)
    private Long memorialId;

    @NotNull
    @Column(name = "added_by", nullable = false)
    private Long addedBy;

    @Column(name = "added_at")
    private OffsetDateTime addedAt;

}