package seg.work.geuliumieum.server.common.repository;

import seg.work.geuliumieum.server.common.entity.GuestbookEntry;

public interface GuestbookEntryRepository extends BaseRepository<GuestbookEntry> {

    long countByUserId(Long userId);
}