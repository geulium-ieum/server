package seg.work.geuliumieum.server.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.MemorialMember;

public interface MemorialMemberRepository extends BaseRepository<MemorialMember> {

    boolean existsByMemorialIdAndUserId(Long memorialId, Long userId);

    Page<MemorialMember> findByUserId(Long userId, Pageable pageable);

    Slice<MemorialMember> findByMemorialId(Long memorialId, Pageable pageable);

    java.util.Optional<MemorialMember> findByMemorialIdAndUserId(Long memorialId, Long userId);

    void deleteByMemorialIdAndUserId(Long memorialId, Long userId);
}