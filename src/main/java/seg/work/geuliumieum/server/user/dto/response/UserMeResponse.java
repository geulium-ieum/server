package seg.work.geuliumieum.server.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import seg.work.geuliumieum.server.common.entity.User;
import seg.work.geuliumieum.server.config.security.UserRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMeResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private UserRole role;
    private String profilePhotoUrl;
    private Boolean isActive;
    private OffsetDateTime lastLoginAt;

    public static UserMeResponse from(User user) {
        return UserMeResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .phone(user.getPhone())
            .role(user.getRole())
            .profilePhotoUrl(user.getProfilePhotoUrl())
            .isActive(user.getIsActive())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }
}
