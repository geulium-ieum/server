package seg.work.geuliumieum.server.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.admin.dto.request.RoleUpdateRequest;
import seg.work.geuliumieum.server.admin.dto.response.AdminUserDetailResponse;
import seg.work.geuliumieum.server.admin.dto.response.AdminUserListItemResponse;
import seg.work.geuliumieum.server.admin.service.AdminUserService;
import seg.work.geuliumieum.server.common.dto.UserInfo;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/user")
@Tag(name = "Admin User", description = "관리자 사용자 관리 API")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "사용자 목록", description = "모든 사용자 목록을 조회합니다(ADMIN|SUPER_ADMIN)")
    @GetMapping("/list")
    public ResponseEntity<Slice<AdminUserListItemResponse>> list(UserInfo admin,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(adminUserService.list(admin, pageable));
    }

    @Operation(summary = "사용자 상세", description = "특정 사용자의 상세(활동 포함)를 조회합니다(ADMIN|SUPER_ADMIN)")
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDetailResponse> get(UserInfo admin,
        @PathVariable("id") Long userId) {
        return ResponseEntity.ok(adminUserService.get(admin, userId));
    }

    @Operation(summary = "사용자 역할 변경", description = "SUPER_ADMIN만 사용자 역할을 변경할 수 있습니다")
    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> changeRole(UserInfo superAdmin,
        @PathVariable("id") Long userId,
        @Valid @RequestBody RoleUpdateRequest request) {
        adminUserService.changeRole(superAdmin, userId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 활성화", description = "사용자를 활성화합니다(ADMIN|SUPER_ADMIN)")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(UserInfo admin, @PathVariable("id") Long userId) {
        adminUserService.activate(admin, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 비활성화", description = "사용자를 비활성화합니다(ADMIN|SUPER_ADMIN)")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(UserInfo admin, @PathVariable("id") Long userId) {
        adminUserService.deactivate(admin, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다(ADMIN|SUPER_ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(UserInfo admin, @PathVariable("id") Long userId) {
        adminUserService.delete(admin, userId);
        return ResponseEntity.ok().build();
    }
}
