package seg.work.geuliumieum.server.common.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.FamilyGroupMemorial;

public interface FamilyGroupMemorialRepository extends BaseRepository<FamilyGroupMemorial> {

    Slice<FamilyGroupMemorial> findByGroupId(Long groupId, Pageable pageable);

    boolean existsByGroupIdAndMemorialId(Long groupId, Long memorialId);

    void deleteByGroupIdAndMemorialId(Long groupId, Long memorialId);
}
