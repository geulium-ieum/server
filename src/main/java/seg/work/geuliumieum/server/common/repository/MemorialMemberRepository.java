package seg.work.geuliumieum.server.common.repository;

import seg.work.geuliumieum.server.common.entity.MemorialMember;

public interface MemorialMemberRepository extends BaseRepository<MemorialMember> {

    boolean existsByMemorialIdAndUserId(Long memorialId, Long userId);
}