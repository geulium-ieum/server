package seg.work.geuliumieum.server.memorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.memorial.constant.STATUS;
import seg.work.geuliumieum.server.memorial.constant.VISIBILITY;

@Getter
@Builder
@Schema(name = "MemorialResponse", description = "추모관 조회 응답")
public class MemorialResponse {

    @Schema(description = "추모관 ID", example = "1001")
    private Long id;
    @Schema(description = "고인명", example = "홍길동")
    private String deceasedName;
    @Schema(description = "생년월일", example = "1950-01-01")
    private LocalDate birthDate;
    @Schema(description = "사망일", example = "2020-12-31")
    private LocalDate deathDate;
    @Schema(description = "장소")
    private String location;
    @Schema(description = "전기/소개")
    private String biography;
    @Schema(description = "대표 사진 URL")
    private String photoUrl;
    @Schema(description = "공개 범위", example = "PUBLIC")
    private VISIBILITY visibility;
    @Schema(description = "상태", example = "PENDING")
    private STATUS status;
    @Schema(description = "생성자 사용자 ID")
    private Long createdBy;
    @Schema(description = "수정자 사용자 ID")
    private Long updatedBy;
    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각")
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
