package seg.work.geuliumieum.server.memorial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.memorial.constant.STATUS;
import seg.work.geuliumieum.server.memorial.constant.VISIBILITY;

@Getter
@Setter
@Schema(name = "MemorialUpdateRequest", description = "추모관 수정 요청")
public class UpdateRequest {

    @Size(max = 100)
    @Schema(description = "고인명", example = "홍길동")
    private String deceasedName;

    @Size(max = 255)
    @Schema(description = "장소")
    private String location;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "생년월일", example = "1950-01-01")
    private LocalDate birthDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "사망일", example = "2020-12-31")
    private LocalDate deathDate;

    @Size(max = 3000)
    @Schema(description = "전기/소개")
    private String biography;

    @Schema(description = "공개 범위", example = "PUBLIC")
    private VISIBILITY visibility;

    @Schema(description = "상태", example = "PENDING")
    private STATUS status;

    public void applyTo(Memorial memorial) {
        if (this.deceasedName != null) {
            memorial.setDeceasedName(this.deceasedName);
        }
        if (this.location != null) {
            memorial.setLocation(this.location);
        }
        if (this.birthDate != null) {
            memorial.setBirthDate(this.birthDate);
        }
        if (this.deathDate != null) {
            memorial.setDeathDate(this.deathDate);
        }
        if (this.biography != null) {
            memorial.setBiography(this.biography);
        }
        if (this.visibility != null) {
            memorial.setVisibility(this.visibility);
        }
        if (this.status != null) {
            memorial.setStatus(this.status);
        }
    }
}
