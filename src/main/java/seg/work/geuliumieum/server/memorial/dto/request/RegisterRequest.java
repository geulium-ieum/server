package seg.work.geuliumieum.server.memorial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(name = "MemorialRegisterRequest", description = "추모관 생성 요청")
public class RegisterRequest {

    @NotBlank(message = "{validation.memorial.deceasedName.notBlank}")
    @Size(max = 100)
    @Schema(description = "고인명", example = "홍길동")
    private String deceasedName;

    @Size(max = 255)
    @Schema(description = "장소")
    private String location;

    @NotNull(message = "{validation.memorial.birthDate.notNull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "생년월일", example = "1950-01-01")
    private LocalDate birthDate;

    @NotNull(message = "{validation.memorial.deathDate.notNull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "사망일", example = "2020-12-31")
    private LocalDate deathDate;

    @Size(max = 3000)
    @Schema(description = "전기/소개")
    private String biography;

    @Schema(description = "공개 범위", example = "PUBLIC")
    private VISIBILITY visibility = VISIBILITY.PUBLIC;

    @Schema(description = "상태", example = "PENDING")
    private STATUS status = STATUS.PENDING;

    public Memorial toEntity() {
        return Memorial.builder()
            .deceasedName(this.getDeceasedName())
            .location(this.getLocation())
            .birthDate(this.getBirthDate())
            .deathDate(this.getDeathDate())
            .biography(this.getBiography())
            .visibility(this.getVisibility())
            .status(this.getStatus())
            .build();
    }
}
