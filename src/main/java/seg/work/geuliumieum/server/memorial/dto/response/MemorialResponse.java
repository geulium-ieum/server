package seg.work.geuliumieum.server.memorial.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.memorial.constant.STATUS;
import seg.work.geuliumieum.server.memorial.constant.VISIBILITY;

@Getter
@Builder
public class MemorialResponse {
    private Long id;
    private String deceasedName;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String location;
    private String biography;
    private String photoUrl;
    private VISIBILITY visibility;
    private STATUS status;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemorialResponse from(Memorial e) {
        return MemorialResponse.builder()
            .id(e.getId())
            .deceasedName(e.getDeceasedName())
            .birthDate(e.getBirthDate())
            .deathDate(e.getDeathDate())
            .location(e.getLocation())
            .biography(e.getBiography())
            .photoUrl(e.getPhotoUrl())
            .visibility(e.getVisibility())
            .status(e.getStatus())
            .createdBy(e.getCreatedBy())
            .updatedBy(e.getUpdatedBy())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }
}
