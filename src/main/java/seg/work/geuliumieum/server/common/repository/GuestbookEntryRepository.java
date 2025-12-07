package seg.work.geuliumieum.server.common.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.GuestbookEntry;

public interface GuestbookEntryRepository extends BaseRepository<GuestbookEntry> {

    long countByUserId(Long userId);

    long countByMemorialId(Long memorialId);

    Slice<GuestbookEntry> findByUserId(Long userId, Pageable pageable);

    Slice<GuestbookEntry> findByMemorialIdAndIsApprovedTrue(Long memorialId, Pageable pageable);
}