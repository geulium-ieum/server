package seg.work.geuliumieum.server.familygroup.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.familygroup.dto.request.AddMemorialRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.FamilyGroupCreateRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.FamilyGroupUpdateRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.InviteRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.MemberRoleUpdateRequest;
import seg.work.geuliumieum.server.familygroup.dto.response.FamilyGroupMemberResponse;
import seg.work.geuliumieum.server.familygroup.dto.response.FamilyGroupResponse;
import seg.work.geuliumieum.server.familygroup.entity.FamilyGroup;
import seg.work.geuliumieum.server.familygroup.entity.FamilyGroupMember;
import seg.work.geuliumieum.server.familygroup.entity.FamilyGroupMemorial;
import seg.work.geuliumieum.server.familygroup.repository.FamilyGroupMemberRepository;
import seg.work.geuliumieum.server.familygroup.repository.FamilyGroupMemorialRepository;
import seg.work.geuliumieum.server.familygroup.repository.FamilyGroupRepository;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;

@Service
@RequiredArgsConstructor
public class FamilyGroupService {

    private final FamilyGroupRepository familyGroupRepository;
    private final FamilyGroupMemberRepository familyGroupMemberRepository;
    private final FamilyGroupMemorialRepository familyGroupMemorialRepository;
    private final MemorialRepository memorialRepository;

    public Slice<FamilyGroupResponse> myGroups(UserInfo user, @ParameterObject Pageable pageable) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        // 소유 그룹
        Slice<FamilyGroup> own = familyGroupRepository.findByOwnerId(user.getId(), pageable);
        // 멤버 그룹
        var memberSlice = familyGroupMemberRepository.findByUserId(user.getId(), pageable);
        List<Long> groupIds = memberSlice.getContent().stream().map(FamilyGroupMember::getGroupId).toList();
        // 소유 + 멤버 합집합으로 간단히 구성 (페이지네이션 단순화)
        List<FamilyGroupResponse> content = own.getContent().stream()
            .map(FamilyGroupResponse::from)
            .collect(Collectors.toList());
        for (Long gid : groupIds) {
            boolean exists = own.getContent().stream().anyMatch(g -> Objects.equals(g.getId(), gid));
            if (!exists) {
                familyGroupRepository.findById(gid).ifPresent(g -> content.add(FamilyGroupResponse.from(g)));
            }
        }
        boolean hasNext = own.hasNext() || memberSlice.hasNext();
        return new SliceImpl<>(content, pageable, hasNext);
    }

    public FamilyGroupResponse get(UserInfo user, Long id) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        FamilyGroup g = familyGroupRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        // 접근: 소유자 또는 멤버만 허용
        if (!Objects.equals(g.getOwnerId(), user.getId())) {
            boolean member = familyGroupMemberRepository.existsByGroupIdAndUserId(id, user.getId());
            if (!member) throw new ApiException(ErrorCode.FORBIDDEN);
        }
        return FamilyGroupResponse.from(g);
    }

    @Transactional
    public void create(UserInfo user, FamilyGroupCreateRequest request) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        FamilyGroup g = new FamilyGroup();
        g.setName(request.getName());
        g.setDescription(request.getDescription());
        g.setOwnerId(user.getId());
        familyGroupRepository.save(g);
    }

    @Transactional
    public void update(UserInfo user, Long id, FamilyGroupUpdateRequest request) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        FamilyGroup g = familyGroupRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(g.getOwnerId(), user.getId())) throw new ApiException(ErrorCode.FORBIDDEN);
        if (request.getName() != null) g.setName(request.getName());
        if (request.getDescription() != null) g.setDescription(request.getDescription());
        familyGroupRepository.save(g);
    }

    @Transactional
    public void delete(UserInfo user, Long id) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        FamilyGroup g = familyGroupRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(g.getOwnerId(), user.getId())) throw new ApiException(ErrorCode.FORBIDDEN);
        familyGroupRepository.delete(g);
    }

    public Slice<FamilyGroupMemberResponse> members(UserInfo user, Long groupId, @ParameterObject Pageable pageable) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        FamilyGroup g = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        // 접근: 소유자 또는 멤버만
        if (!Objects.equals(g.getOwnerId(), user.getId()) &&
            !familyGroupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        return familyGroupMemberRepository.findByGroupId(groupId, pageable).map(FamilyGroupMemberResponse::from);
    }

    @Transactional
    public void invite(UserInfo user, Long groupId, InviteRequest request) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        FamilyGroup g = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        // 초대는 소유자만 허용 (간단 정책)
        if (!Objects.equals(g.getOwnerId(), user.getId())) throw new ApiException(ErrorCode.FORBIDDEN);
        if (familyGroupMemberRepository.existsByGroupIdAndUserId(groupId, request.getUserId())) {
            return; // 이미 멤버면 무시
        }
        FamilyGroupMember m = new FamilyGroupMember();
        m.setGroupId(groupId);
        m.setUserId(request.getUserId());
        m.setRole(request.getRole() == null ? "member" : request.getRole());
        familyGroupMemberRepository.save(m);
    }

    @Transactional
    public void join(UserInfo user, Long groupId) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (familyGroupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) return;
        FamilyGroupMember m = new FamilyGroupMember();
        m.setGroupId(groupId);
        m.setUserId(user.getId());
        m.setRole("member");
        familyGroupMemberRepository.save(m);
    }

    @Transactional
    public void removeMember(UserInfo user, Long groupId, Long targetUserId) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        FamilyGroup g = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(g.getOwnerId(), user.getId())) throw new ApiException(ErrorCode.FORBIDDEN);
        // 소유자 본인은 제거 불가
        if (Objects.equals(targetUserId, g.getOwnerId())) throw new ApiException(ErrorCode.FORBIDDEN);
        familyGroupMemberRepository.deleteByGroupIdAndUserId(groupId, targetUserId);
    }

    @Transactional
    public void changeRole(UserInfo user, Long groupId, Long targetUserId, MemberRoleUpdateRequest request) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        FamilyGroup g = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(g.getOwnerId(), user.getId())) throw new ApiException(ErrorCode.FORBIDDEN);
        FamilyGroupMember m = familyGroupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        m.setRole(request.getRole());
        familyGroupMemberRepository.save(m);
    }

    public Slice<MemorialResponse> groupMemorials(UserInfo user, Long groupId, @ParameterObject Pageable pageable) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        FamilyGroup g = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(g.getOwnerId(), user.getId()) &&
            !familyGroupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        var linkSlice = familyGroupMemorialRepository.findByGroupId(groupId, pageable);
        List<Long> memorialIds = linkSlice.getContent().stream().map(FamilyGroupMemorial::getMemorialId).toList();
        List<MemorialResponse> content = memorialIds.stream()
            .map(mid -> memorialRepository.findById(mid).orElse(null))
            .filter(Objects::nonNull)
            .map(MemorialResponse::from)
            .collect(Collectors.toList());
        return new SliceImpl<>(content, pageable, linkSlice.hasNext());
    }

    @Transactional
    public void addMemorial(UserInfo user, Long groupId, AddMemorialRequest request) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        FamilyGroup g = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(g.getOwnerId(), user.getId())) throw new ApiException(ErrorCode.FORBIDDEN);
        memorialRepository.findById(request.getMemorialId()).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (familyGroupMemorialRepository.existsByGroupIdAndMemorialId(groupId, request.getMemorialId())) return;
        FamilyGroupMemorial link = new FamilyGroupMemorial();
        link.setGroupId(groupId);
        link.setMemorialId(request.getMemorialId());
        familyGroupMemorialRepository.save(link);
    }

    @Transactional
    public void removeMemorial(UserInfo user, Long groupId, Long memorialId) {
        if (user == null || user.getId() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        FamilyGroup g = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(g.getOwnerId(), user.getId())) throw new ApiException(ErrorCode.FORBIDDEN);
        familyGroupMemorialRepository.deleteByGroupIdAndMemorialId(groupId, memorialId);
    }
}
