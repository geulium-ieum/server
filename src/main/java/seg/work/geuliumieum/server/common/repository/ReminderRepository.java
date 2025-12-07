package seg.work.geuliumieum.server.common.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.Reminder;

public interface ReminderRepository extends BaseRepository<Reminder> {

    Slice<Reminder> findByUserId(Long userId, Pageable pageable);

    Slice<Reminder> findByMemorialId(Long memorialId, Pageable pageable);

    Slice<Reminder> findByUserIdAndMemorialId(Long userId, Long memorialId, Pageable pageable);

    List<Reminder> findByUserIdAndIsActiveTrue(Long userId);
}
