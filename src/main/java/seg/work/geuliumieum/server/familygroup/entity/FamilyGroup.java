package seg.work.geuliumieum.server.familygroup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import seg.work.geuliumieum.server.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "family_groups")
@Comment("가족 그룹 테이블")
public class FamilyGroup extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -8123456789055512345L;

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @CreatedBy
    @Column(name = "owner_id", updatable = false)
    private Long ownerId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
