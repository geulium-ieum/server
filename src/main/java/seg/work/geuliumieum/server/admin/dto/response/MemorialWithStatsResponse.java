package seg.work.geuliumieum.server.admin.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import seg.work.geuliumieum.server.common.entity.MemorialWithStatsView;
import seg.work.geuliumieum.server.memorial.constant.STATUS;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemorialWithStatsResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long memorialId;
    private String deceasedName;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String biography;
    private String photoUrl;
    private String visibility;
    private String location;
    private STATUS status;
    private LocalDateTime createdAt;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long tributeCount;
    private Long offeringCount;
    private Long memberCount;

    public static MemorialWithStatsResponse toResponse(MemorialWithStatsView v) {
        return MemorialWithStatsResponse.builder()
            .memorialId(v.getMemorialId())
            .deceasedName(v.getDeceasedName())
            .birthDate(v.getBirthDate())
            .deathDate(v.getDeathDate())
            .biography(v.getBiography())
            .photoUrl(v.getPhotoUrl())
            .visibility(v.getVisibility())
            .location(v.getLocation())
            .status(v.getStatus())
            .createdAt(v.getCreatedAt())
            .createdBy(v.getCreatedBy())
            .tributeCount(v.getTributeCount())
            .offeringCount(v.getOfferingCount())
            .memberCount(v.getMemberCount())
            .build();
    }
}
