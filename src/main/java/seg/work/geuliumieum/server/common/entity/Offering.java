package seg.work.geuliumieum.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;

@Getter
@Setter
@Entity
@Table(name = "offerings")
@Comment("헌화/분향/헌촛 테이블")
public class Offering extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -8168487875840555185L;

    @NotNull
    @Column(name = "memorial_id", nullable = false)
    private Long memorialId;

    @NotNull
    @CreatedBy
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "offering_type", nullable = false, length = 20)
    private String offeringType;

    @Column(name = "message", length = Integer.MAX_VALUE)
    private String message;

}