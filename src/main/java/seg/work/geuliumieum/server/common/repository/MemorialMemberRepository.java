package seg.work.geuliumieum.server.common.repository;

import seg.work.geuliumieum.server.common.entity.MemorialMember;

public interface MemorialMemberRepository extends BaseRepository<MemorialMember> {

    boolean existsByMemorialIdAndUserId(Long memorialId, Long userId);

    org.springframework.data.domain.Page<MemorialMember> findByUserId(Long userId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<MemorialMember> findByMemorialId(Long memorialId, org.springframework.data.domain.Pageable pageable);

    java.util.Optional<MemorialMember> findByMemorialIdAndUserId(Long memorialId, Long userId);

    void deleteByMemorialIdAndUserId(Long memorialId, Long userId);
}