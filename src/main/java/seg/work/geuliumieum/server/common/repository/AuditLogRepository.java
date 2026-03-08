package seg.work.geuliumieum.server.common.repository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.entity.AuditLog;

public interface AuditLogRepository extends BaseRepository<AuditLog> {

    @Modifying
    @Transactional
    @Query("DELETE FROM AuditLog a WHERE a.createdAt < :cutoff")
    int deleteOldLogs(LocalDateTime cutoff);

}