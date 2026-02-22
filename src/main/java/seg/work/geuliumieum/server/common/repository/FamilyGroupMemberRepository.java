package seg.work.geuliumieum.server.common.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.entity.FamilyGroupMember;

public interface FamilyGroupMemberRepository extends BaseRepository<FamilyGroupMember> {

    Slice<FamilyGroupMember> findByGroupId(Long groupId, Pageable pageable);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByUserIdAndGroupIdIn(Long userId, Collection<Long> groupIds);

    void deleteByGroupIdAndUserId(Long groupId, Long userId);

    List<FamilyGroupMember> findByUserId(Long userId);

    java.util.Optional<FamilyGroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
}
