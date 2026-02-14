package seg.work.geuliumieum.server.common.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

/**
 * Read-only JPA entity mapped to PostgreSQL view `vw_memorial_with_stats`.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vw_memorial_with_stats")
@Immutable
public class MemorialWithStatsView {

    @Id
    @Column(name = "memorial_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long memorialId;

    @Column(name = "deceased_name")
    private String deceasedName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "death_date")
    private LocalDate deathDate;

    @Column(name = "biography")
    private String biography;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "visibility")
    private String visibility;

    @Column(name = "tribute_count")
    private Long tributeCount;

    @Column(name = "offering_count")
    private Long offeringCount;

    @Column(name = "member_count")
    private Long memberCount;
}
