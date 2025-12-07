package seg.work.geuliumieum.server.familygroup.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.repository.BaseRepository;
import seg.work.geuliumieum.server.familygroup.entity.FamilyGroupMemorial;

public interface FamilyGroupMemorialRepository extends BaseRepository<FamilyGroupMemorial> {

    Slice<FamilyGroupMemorial> findByGroupId(Long groupId, Pageable pageable);

    boolean existsByGroupIdAndMemorialId(Long groupId, Long memorialId);

    void deleteByGroupIdAndMemorialId(Long groupId, Long memorialId);
}
