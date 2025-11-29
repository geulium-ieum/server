package seg.work.geuliumieum.server.admin.controller;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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

/**
 * 관리자용 감사 로그 조회 API.
 * - action/targetType/userId/기간으로 필터링하고 페이지네이션하여 조회합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    /**
     * 감사 로그 목록을 조회합니다.
     *
     * @param action 감사 액션 필터 (CREATE/UPDATE/DELETE/LOGIN/LOGOUT)
     * @param targetType 대상 엔티티 타입(단순 클래스명) 필터
     * @param userId 수행자 사용자 ID 필터
     * @param from 시작 시각(포함)
     * @param to 종료 시각(포함)
     * @param pageable 페이지/정렬 정보
     */
    @GetMapping
    public Page<AdminAuditLogResponse> list(
        @RequestParam(required = false) AuditAction action,
        @RequestParam(required = false) String targetType,
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        @ParameterObject Pageable pageable
    ) {
        return adminAuditLogService.search(action, targetType, userId, from, to, pageable);
    }
}
