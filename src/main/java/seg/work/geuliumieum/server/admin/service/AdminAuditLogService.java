package seg.work.geuliumieum.server.admin.service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.admin.dto.response.AdminAuditLogResponse;
import seg.work.geuliumieum.server.common.audit.AuditAction;
import seg.work.geuliumieum.server.common.entity.AuditLog;
import seg.work.geuliumieum.server.common.repository.AuditLogRepository;

@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public Page<AdminAuditLogResponse> search(AuditAction action, String targetType, Long userId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<AuditLog> spec = buildSpec(action, targetType, userId, from, to);

        return auditLogRepository.findAll(spec, pageable).map(AdminAuditLogResponse::from);
    }

    private Specification<AuditLog> buildSpec(AuditAction action, String targetType, Long userId, LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action.name()));
            }
            if (targetType != null && !targetType.isBlank()) {
                predicates.add(cb.equal(root.get("targetType"), targetType));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
