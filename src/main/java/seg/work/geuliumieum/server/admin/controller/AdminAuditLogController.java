package seg.work.geuliumieum.server.admin.controller;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.admin.dto.response.AdminAuditLogResponse;
import seg.work.geuliumieum.server.admin.service.AdminAuditLogService;
import seg.work.geuliumieum.server.common.audit.AuditAction;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    @GetMapping
    public Page<AdminAuditLogResponse> list(
        @RequestParam(required = false) AuditAction action,
        @RequestParam(required = false) String targetType,
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        Pageable pageable
    ) {
        return adminAuditLogService.search(action, targetType, userId, from, to, pageable);
    }
}
