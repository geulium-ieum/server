package seg.work.geuliumieum.server.common.repository;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.Announcement;

public interface AnnouncementRepository extends BaseRepository<Announcement> {

    Slice<Announcement> findByIsPublishedTrueOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);

    Slice<Announcement> findByIsPublishedTrueAndIsPinnedTrueOrderByCreatedAtDesc(Pageable pageable);

    Optional<Announcement> findByIdAndIsPublishedTrue(Long id);
}