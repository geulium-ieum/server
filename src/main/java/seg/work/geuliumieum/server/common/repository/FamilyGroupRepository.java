package seg.work.geuliumieum.server.common.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.FamilyGroup;

public interface FamilyGroupRepository extends BaseRepository<FamilyGroup> {

    Slice<FamilyGroup> findByOwnerId(Long ownerId, Pageable pageable);
}
