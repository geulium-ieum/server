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
@Schema(name = "AdminUserDetailResponse", description = "관리자: 사용자 상세(활동 포함)")
public class AdminUserDetailResponse {

    // 기본 정보
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

    public static AdminUserDetailResponse from(User u, long tribute, long offering, long guestbook) {
        return AdminUserDetailResponse.builder()
            .id(u.getId())
            .email(u.getEmail())
            .name(u.getName())
            .phone(u.getPhone())
            .role(u.getRole())
            .profilePhotoUrl(u.getProfilePhotoUrl())
            .isActive(u.getIsActive())
            .lastLoginAt(u.getLastLoginAt())
            .createdAt(u.getCreatedAt())
            .updatedAt(u.getUpdatedAt())
            .tributeCount(tribute)
            .offeringCount(offering)
            .guestbookCount(guestbook)
            .build();
    }
}
