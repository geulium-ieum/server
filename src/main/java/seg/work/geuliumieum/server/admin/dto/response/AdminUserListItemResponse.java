package seg.work.geuliumieum.server.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;
import seg.work.geuliumieum.server.common.entity.User;
import seg.work.geuliumieum.server.config.security.UserRole;

@Getter
@Builder
@Schema(name = "AdminUserListItemResponse", description = "관리자: 사용자 목록 아이템")
public class AdminUserListItemResponse {

    private Long id;
    private String email;
    private String name;
    private UserRole role;
    private Boolean isActive;
    private OffsetDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public static AdminUserListItemResponse from(User u) {
        return AdminUserListItemResponse.builder()
            .id(u.getId())
            .email(u.getEmail())
            .name(u.getName())
            .role(u.getRole())
            .isActive(u.getIsActive())
            .lastLoginAt(u.getLastLoginAt())
            .createdAt(u.getCreatedAt())
            .build();
    }
}
