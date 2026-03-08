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
@Schema(name = "AdminUserDetailResponse", description = "관리자: 사용자 상세(활동 포함)")
public class AdminUserDetailResponse {

    // 기본 정보
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String email;
    private String name;
    private String phone;
    private UserRole role;
    private String profilePhotoUrl;
    private Boolean isActive;
    private OffsetDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 활동 통계
    private long tributeCount;
    private long offeringCount;
    private long guestbookCount;

    public static AdminUserDetailResponse from(User user, long tribute, long offering, long guestbook) {
        return AdminUserDetailResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .phone(user.getPhone())
            .role(user.getRole())
            .profilePhotoUrl(user.getProfilePhotoUrl())
            .isActive(user.getIsActive())
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .tributeCount(tribute)
            .offeringCount(offering)
            .guestbookCount(guestbook)
            .build();
    }
}
