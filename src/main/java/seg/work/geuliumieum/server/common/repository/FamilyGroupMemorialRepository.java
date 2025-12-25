package seg.work.geuliumieum.server.common.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.FamilyGroupMemorial;

public interface FamilyGroupMemorialRepository extends BaseRepository<FamilyGroupMemorial> {

    Slice<FamilyGroupMemorial> findByGroupId(Long groupId, Pageable pageable);

    List<FamilyGroupMemorial> findAllByMemorialId(Long memorialId);

    boolean existsByGroupIdAndMemorialId(Long groupId, Long memorialId);

    void deleteByGroupIdAndMemorialId(Long groupId, Long memorialId);
}
