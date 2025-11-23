package seg.work.geuliumieum.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;

@Getter
@Setter
@Entity
@Table(name = "album_photos")
public class AlbumPhoto extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -2435138201009276929L;

    @NotNull
    @Column(name = "album_id", nullable = false)
    private Long albumId;

    @NotNull
    @Column(name = "photo_url", nullable = false, length = Integer.MAX_VALUE)
    private String photoUrl;

    @Column(name = "caption", length = Integer.MAX_VALUE)
    private String caption;

    @NotNull
    @CreatedBy
    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

}