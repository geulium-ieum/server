package seg.work.geuliumieum.server.admin.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String email;
    private String name;
    private UserRole role;
    private Boolean isActive;
    private OffsetDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public static AdminUserListItemResponse from(User user) {
        return AdminUserListItemResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .role(user.getRole())
            .isActive(user.getIsActive())
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
