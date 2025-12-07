package seg.work.geuliumieum.server.familygroup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import seg.work.geuliumieum.server.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "family_group_memorials")
@Comment("가족 그룹 - 추모관 연결 테이블")
public class FamilyGroupMemorial extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -551234567800998877L;

    @NotNull
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @NotNull
    @Column(name = "memorial_id", nullable = false)
    private Long memorialId;
}
