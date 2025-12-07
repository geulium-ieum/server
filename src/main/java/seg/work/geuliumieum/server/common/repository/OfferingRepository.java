package seg.work.geuliumieum.server.common.repository;

import seg.work.geuliumieum.server.common.entity.Offering;

public interface OfferingRepository extends BaseRepository<Offering> {

    long countByUserId(Long userId);
}