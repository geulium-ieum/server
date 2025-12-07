package seg.work.geuliumieum.server.familygroup.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seg.work.geuliumieum.server.common.repository.BaseRepository;
import seg.work.geuliumieum.server.familygroup.entity.FamilyGroupMember;

public interface FamilyGroupMemberRepository extends BaseRepository<FamilyGroupMember> {

    Slice<FamilyGroupMember> findByGroupId(Long groupId, Pageable pageable);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    void deleteByGroupIdAndUserId(Long groupId, Long userId);

    Slice<FamilyGroupMember> findByUserId(Long userId, Pageable pageable);

    java.util.Optional<FamilyGroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
}
