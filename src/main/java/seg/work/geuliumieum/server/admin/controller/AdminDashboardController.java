package seg.work.geuliumieum.server.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.admin.dto.response.ActiveItemResponse;
import seg.work.geuliumieum.server.admin.dto.response.AdminUserListItemResponse;
import seg.work.geuliumieum.server.admin.dto.response.SystemStatsResponse;
import seg.work.geuliumieum.server.admin.service.AdminDashboardService;
import seg.work.geuliumieum.server.common.dto.UserInfo;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "관리자 대시보드 통계 API")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "전체 통계", description = "전체 누적 통계를 조회합니다(ADMIN|SUPER_ADMIN)")
    @GetMapping("/stats")
    public ResponseEntity<SystemStatsResponse> stats(UserInfo admin) {
        return ResponseEntity.ok(adminDashboardService.stats(admin));
    }

    @Operation(summary = "최근 가입 사용자", description = "최근 가입 사용자를 최신순으로 조회합니다(ADMIN|SUPER_ADMIN)")
    @GetMapping("/recent-user")
    public ResponseEntity<Slice<AdminUserListItemResponse>> recentUsers(UserInfo admin,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(adminDashboardService.recentUsers(admin, pageable));
    }

    @Operation(summary = "활동 많은 추모관(30일)", description = "최근 n일 기준 활동(추모글 수)이 많은 추모관 목록을 조회합니다(ADMIN|SUPER_ADMIN)")
    @GetMapping("/active-memorial")
    public ResponseEntity<List<ActiveItemResponse>> activeMemorials(UserInfo admin,
        @RequestParam(name = "days", required = false, defaultValue = "30") int days) {
        return ResponseEntity.ok(adminDashboardService.activeMemorials(admin, days));
    }

    @Operation(summary = "활동 많은 사용자(30일)", description = "최근 n일 기준 활동(추모글 수)이 많은 사용자 목록을 조회합니다(ADMIN|SUPER_ADMIN)")
    @GetMapping("/active-user")
    public ResponseEntity<List<ActiveItemResponse>> activeUsers(UserInfo admin,
        @RequestParam(name = "days", required = false, defaultValue = "30") int days) {
        return ResponseEntity.ok(adminDashboardService.activeUsers(admin, days));
    }
}
