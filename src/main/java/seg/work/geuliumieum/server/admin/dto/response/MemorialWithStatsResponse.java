package seg.work.geuliumieum.server.admin.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import seg.work.geuliumieum.server.common.entity.MemorialWithStatsView;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemorialWithStatsResponse {

    private Long memorialId;
    private String deceasedName;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String biography;
    private String photoUrl;
    private String visibility;
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
            .tributeCount(v.getTributeCount())
            .offeringCount(v.getOfferingCount())
            .memberCount(v.getMemberCount())
            .build();
    }
}
