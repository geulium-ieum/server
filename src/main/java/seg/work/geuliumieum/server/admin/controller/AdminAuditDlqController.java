package seg.work.geuliumieum.server.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.admin.dto.response.DlqReprocessResponse;
import seg.work.geuliumieum.server.admin.service.AdminAuditDlqService;

/**
 * 감사 DLQ(Dead Letter Queue) 운영 API. - DLQ 길이 확인, 재처리(메인 스트림 재투입), 퍼지(삭제) 기능을 제공합니다. SUPER_ADMIN 전용입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/audit-logs/dlq")
public class AdminAuditDlqController {

    private final AdminAuditDlqService dlqService;

    /**
     * DLQ 현재 길이(적재된 레코드 수)를 조회합니다.
     */
    @GetMapping("/size")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Long> size() {
        return ResponseEntity.ok(dlqService.size());
    }

    /**
     * DLQ 상위 N건을 메인 스트림으로 재투입하고, 성공한 건은 DLQ에서 삭제합니다.
     */
    @PostMapping("/reprocess")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<DlqReprocessResponse> reprocess(@RequestParam(name = "max", defaultValue = "100") int max) {
        DlqReprocessResponse res = dlqService.reprocess(Math.max(1, Math.min(max, 10_000)));
        return ResponseEntity.ok(res);
    }

    /**
     * DLQ를 전체 또는 부분 삭제합니다. max 미지정 시 전체 삭제합니다.
     */
    @DeleteMapping("/purge")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Long> purge(@RequestParam(name = "max", required = false) Integer max) {
        long purged = dlqService.purge(max);
        return ResponseEntity.ok(purged);
    }
}
