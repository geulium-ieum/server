package seg.work.geuliumieum.server.memorial.dto.request;

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
public class RegisterRequest {

    @NotBlank(message = "고인명은 필수값 입니다.")
    @Size(max = 100)
    private String deceasedName;

    @Size(max = 255)
    private String location;

    @NotNull(message = "고인의 생년월일은 필수값 입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull(message = "고인의 사망일은 필수값 입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deathDate;

    @Size(max = 3000)
    private String biography;

    private VISIBILITY visibility = VISIBILITY.PUBLIC;

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
