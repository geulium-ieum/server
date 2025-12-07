package seg.work.geuliumieum.server.admin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.admin.dto.request.RejectRequest;
import seg.work.geuliumieum.server.admin.dto.response.MemorialWithStatsResponse;
import seg.work.geuliumieum.server.admin.service.AdminMemorialService;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;

/**
 * 관리자용 추모관(Admin) API. - 특정 추모관의 상세 정보와 통계 데이터를 함께 조회합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/memorial")
@Tag(name = "Admin Memorial", description = "관리자용 추모관 API")
public class AdminMemorialController {

    private final AdminMemorialService adminMemorialService;

    /**
     * 추모관 상세(통계 포함)를 조회합니다.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MemorialWithStatsResponse> getMemorialWithStats(@PathVariable("id") Long memorialId) {
        MemorialWithStatsResponse body = adminMemorialService.getMemorialWithStats(memorialId);
        return ResponseEntity.ok(body);
    }

    /**
     * 승인 대기 중인 추모관 목록을 조회합니다.
     */
    @GetMapping("/pending/list")
    public ResponseEntity<Slice<MemorialResponse>> pendingList(UserInfo user,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(adminMemorialService.getPendingList(user, pageable));
    }

    /**
     * 모든 추모관 목록(상태 무관)을 조회합니다.
     */
    @GetMapping("/all")
    public ResponseEntity<Slice<MemorialResponse>> all(UserInfo user,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(adminMemorialService.getAll(user, pageable));
    }

    /**
     * 추모관을 승인 처리합니다.
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approve(UserInfo user, @PathVariable("id") Long id) {
        adminMemorialService.approve(user, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 추모관을 반려 처리합니다.
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> reject(UserInfo user,
        @PathVariable("id") Long id,
        @RequestBody(required = false) RejectRequest request) {
        adminMemorialService.reject(user, id, request == null ? null : request.getReason());
        return ResponseEntity.ok().build();
    }
}
