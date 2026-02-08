package seg.work.geuliumieum.server.memorial.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.memorial.constant.STATUS;
import seg.work.geuliumieum.server.memorial.constant.VISIBILITY;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MemorialResponse", description = "추모관 조회 응답")
public class MemorialResponse {

    @JsonSerialize(using = ToStringSerializer.class)
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

    public static MemorialResponse from(Memorial memorial) {
        return MemorialResponse.builder()
            .id(memorial.getId())
            .deceasedName(memorial.getDeceasedName())
            .birthDate(memorial.getBirthDate())
            .deathDate(memorial.getDeathDate())
            .location(memorial.getLocation())
            .biography(memorial.getBiography())
            .photoUrl(memorial.getPhotoUrl())
            .visibility(memorial.getVisibility())
            .status(memorial.getStatus())
            .createdBy(memorial.getCreatedBy())
            .updatedBy(memorial.getUpdatedBy())
            .createdAt(memorial.getCreatedAt())
            .updatedAt(memorial.getUpdatedAt())
            .build();
    }
}
