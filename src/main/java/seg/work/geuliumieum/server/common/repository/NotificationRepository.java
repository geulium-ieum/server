package seg.work.geuliumieum.server.common.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seg.work.geuliumieum.server.common.entity.Notification;

public interface NotificationRepository extends BaseRepository<Notification> {

    Slice<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("update Notification n set n.isRead = true where n.userId = :userId and (n.isRead = false or n.isRead is null)")
    int markAllRead(@Param("userId") Long userId);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndId(Long userId, Long id);
}