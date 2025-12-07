package seg.work.geuliumieum.server.familygroup.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import java.util.List;
import seg.work.geuliumieum.server.common.repository.BaseRepository;
import seg.work.geuliumieum.server.familygroup.entity.FamilyGroup;

public interface FamilyGroupRepository extends BaseRepository<FamilyGroup> {

    Slice<FamilyGroup> findByOwnerId(Long ownerId, Pageable pageable);

    Slice<FamilyGroup> findByIdIn(List<Long> ids, Pageable pageable);
}
