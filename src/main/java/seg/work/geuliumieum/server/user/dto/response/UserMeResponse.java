package seg.work.geuliumieum.server.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "UserMeResponse", description = "현재 사용자 프로필 응답")
public class UserMeResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "휴대전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "권한 역할")
    private UserRole role;

    @Schema(description = "프로필 사진 URL")
    private String profilePhotoUrl;

    @Schema(description = "활성 여부")
    private Boolean isActive;

    @Schema(description = "마지막 로그인 시각")
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
