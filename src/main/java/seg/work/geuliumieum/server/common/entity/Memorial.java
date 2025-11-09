package seg.work.geuliumieum.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "memorials")
@Comment("추모관 정보 테이블")
public class Memorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "deceased_name", nullable = false, length = 100)
    private String deceasedName;

    @NotNull
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @NotNull
    @Column(name = "death_date", nullable = false)
    private LocalDate deathDate;

    @Size(max = 255)
    @Column(name = "location")
    private String location;

    @Column(name = "biography", length = Integer.MAX_VALUE)
    private String biography;

    @Column(name = "photo_url", length = Integer.MAX_VALUE)
    private String photoUrl;

    @Size(max = 20)
    @ColumnDefault("'public'")
    @Column(name = "visibility", length = 20)
    private String visibility;

    @Size(max = 20)
    @ColumnDefault("'pending'")
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "rejection_reason", length = Integer.MAX_VALUE)
    private String rejectionReason;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

}