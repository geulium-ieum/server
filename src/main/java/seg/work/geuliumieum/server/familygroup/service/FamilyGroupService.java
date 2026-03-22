package seg.work.geuliumieum.server.familygroup.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.FamilyGroup;
import seg.work.geuliumieum.server.common.entity.FamilyGroupMember;
import seg.work.geuliumieum.server.common.entity.FamilyGroupMemorial;
import seg.work.geuliumieum.server.common.entity.User;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.mail.MailClient;
import seg.work.geuliumieum.server.common.repository.FamilyGroupMemberRepository;
import seg.work.geuliumieum.server.common.repository.FamilyGroupMemorialRepository;
import seg.work.geuliumieum.server.common.repository.FamilyGroupRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.common.repository.UserRepository;
import seg.work.geuliumieum.server.familygroup.dto.request.AddMemorialRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.FamilyGroupCreateRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.FamilyGroupUpdateRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.InviteRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.MemberRoleUpdateRequest;
import seg.work.geuliumieum.server.familygroup.dto.response.FamilyGroupMemberResponse;
import seg.work.geuliumieum.server.familygroup.dto.response.FamilyGroupResponse;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;
import seg.work.geuliumieum.server.util.RedisUtil;

@Service
@RequiredArgsConstructor
public class FamilyGroupService {

    private final FamilyGroupRepository familyGroupRepository;
    private final FamilyGroupMemberRepository familyGroupMemberRepository;
    private final FamilyGroupMemorialRepository familyGroupMemorialRepository;
    private final MemorialRepository memorialRepository;
    private final UserRepository userRepository;
    private final MailClient mailClient;

    private static final String INVITE_KEY_PREFIX = "family:invite:";

    public Slice<FamilyGroupResponse> myGroups(UserInfo userInfo, @ParameterObject Pageable pageable) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        // 소유 그룹
        Slice<FamilyGroup> own = familyGroupRepository.findByOwnerId(userInfo.getId(), pageable);
        // 멤버 그룹
        List<FamilyGroupMember> memberSlice = familyGroupMemberRepository.findByUserId(userInfo.getId());
        List<Long> groupIds = memberSlice.stream().map(FamilyGroupMember::getGroupId).toList();
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
        return new SliceImpl<>(content, pageable, own.hasNext());
    }

    @Cacheable(cacheNames = "family:detail", key = "#id")
    public FamilyGroupResponse get(UserInfo userInfo, Long id) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        FamilyGroup familyGroup = familyGroupRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        // 접근: 소유자 또는 멤버만 허용
        if (!Objects.equals(familyGroup.getOwnerId(), userInfo.getId())) {
            boolean member = familyGroupMemberRepository.existsByGroupIdAndUserId(id, userInfo.getId());
            if (!member) {
                throw new ApiException(ErrorCode.FORBIDDEN);
            }
        }
        return FamilyGroupResponse.from(familyGroup);
    }

    @Transactional
    public void create(UserInfo userInfo, FamilyGroupCreateRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        FamilyGroup familyGroup = new FamilyGroup();
        familyGroup.setName(request.getName());
        familyGroup.setDescription(request.getDescription());
        familyGroup.setOwnerId(userInfo.getId());
        familyGroupRepository.save(familyGroup);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "family:detail", key = "#id"),
    })
    public void update(UserInfo userInfo, Long id, FamilyGroupUpdateRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        FamilyGroup g = familyGroupRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(g.getOwnerId(), userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (request.getName() != null) {
            g.setName(request.getName());
        }
        if (request.getDescription() != null) {
            g.setDescription(request.getDescription());
        }
        familyGroupRepository.save(g);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "family:detail", key = "#id"),
    })
    public void delete(UserInfo userInfo, Long id) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        FamilyGroup familyGroup = familyGroupRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(familyGroup.getOwnerId(), userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        familyGroupRepository.delete(familyGroup);
    }

    public Slice<FamilyGroupMemberResponse> members(UserInfo userInfo, Long groupId, @ParameterObject Pageable pageable) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        FamilyGroup familyGroup = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        // 접근: 소유자 또는 멤버만
        if (!Objects.equals(familyGroup.getOwnerId(), userInfo.getId()) &&
            !familyGroupMemberRepository.existsByGroupIdAndUserId(groupId, userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        return familyGroupMemberRepository.findByGroupId(groupId, pageable).map(FamilyGroupMemberResponse::from);
    }

    @Transactional
    @CacheEvict(cacheNames = "memorial:access", allEntries = true)
    public void invite(UserInfo userInfo, Long groupId, InviteRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        FamilyGroup familyGroup = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        // 초대는 소유자만 허용 (간단 정책)
        if (!Objects.equals(familyGroup.getOwnerId(), userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (familyGroupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            return; // 이미 멤버면 무시
        }
        Long joinUserId = RedisUtil.getLongValue(INVITE_KEY_PREFIX + groupId);
        if (joinUserId != null && Objects.equals(joinUserId, userInfo.getId())) {
            throw new ApiException(ErrorCode.ALREADY_INVITATION);
        } else {
            // 알림 및 이메일 발송
            RedisUtil.setWithExpiryMin(INVITE_KEY_PREFIX + groupId, user.getId(), 5);
            mailClient.sendInvitationEmail(user.getEmail(), user.getName(), familyGroup.getName(), groupId);
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "memorial:access", allEntries = true)
    public void join(UserInfo userInfo, Long groupId) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (familyGroupMemberRepository.existsByGroupIdAndUserId(groupId, userInfo.getId())) {
            return;
        }

        Long joinUserId = RedisUtil.getLongValue(INVITE_KEY_PREFIX + groupId);
        if (joinUserId == null) {
            throw new ApiException(ErrorCode.EXPIRED_INVITATION);
        } else if (Objects.equals(userInfo.getId(), joinUserId)) {
            throw new ApiException(ErrorCode.INVALID_INVITATION);
        } else {
            FamilyGroupMember familyGroupMember = new FamilyGroupMember();
            familyGroupMember.setGroupId(groupId);
            familyGroupMember.setUserId(userInfo.getId());
            familyGroupMember.setRole("member");
            familyGroupMember.setJoinedAt(OffsetDateTime.now());
            familyGroupMemberRepository.save(familyGroupMember);
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "memorial:access", allEntries = true)
    public void removeMember(UserInfo userInfo, Long groupId, Long targetUserId) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        FamilyGroup familyGroup = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(familyGroup.getOwnerId(), userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        // 소유자 본인은 제거 불가
        if (Objects.equals(targetUserId, familyGroup.getOwnerId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        familyGroupMemberRepository.deleteByGroupIdAndUserId(groupId, targetUserId);
    }

    @Transactional
    @CacheEvict(cacheNames = "memorial:access", allEntries = true)
    public void changeRole(UserInfo userInfo, Long groupId, Long targetUserId, MemberRoleUpdateRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        FamilyGroup familyGroup = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(familyGroup.getOwnerId(), userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        FamilyGroupMember familyGroupMember = familyGroupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        familyGroupMember.setRole(request.getRole());
        familyGroupMemberRepository.save(familyGroupMember);
    }

    public Slice<MemorialResponse> groupMemorials(UserInfo userInfo, Long groupId, @ParameterObject Pageable pageable) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        FamilyGroup familyGroup = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(familyGroup.getOwnerId(), userInfo.getId()) &&
            !familyGroupMemberRepository.existsByGroupIdAndUserId(groupId, userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        Slice<FamilyGroupMemorial> linkSlice = familyGroupMemorialRepository.findByGroupId(groupId, pageable);
        List<Long> memorialIds = linkSlice.getContent().stream().map(FamilyGroupMemorial::getMemorialId).toList();
        List<MemorialResponse> content = memorialIds.stream()
            .map(mid -> memorialRepository.findById(mid).orElse(null))
            .filter(Objects::nonNull)
            .map(MemorialResponse::from)
            .collect(Collectors.toList());
        return new SliceImpl<>(content, pageable, linkSlice.hasNext());
    }

    @Transactional
    @CacheEvict(cacheNames = "memorial:access", allEntries = true)
    public void addMemorial(UserInfo userInfo, Long groupId, AddMemorialRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        FamilyGroup familyGroup = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(familyGroup.getOwnerId(), userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        memorialRepository.findById(request.getMemorialId()).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (familyGroupMemorialRepository.existsByGroupIdAndMemorialId(groupId, request.getMemorialId())) {
            return;
        }
        FamilyGroupMemorial link = new FamilyGroupMemorial();
        link.setGroupId(groupId);
        link.setMemorialId(request.getMemorialId());
        familyGroupMemorialRepository.save(link);
    }

    @Transactional
    @CacheEvict(cacheNames = "memorial:access", allEntries = true)
    public void removeMemorial(UserInfo userInfo, Long groupId, Long memorialId) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        FamilyGroup familyGroup = familyGroupRepository.findById(groupId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(familyGroup.getOwnerId(), userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        familyGroupMemorialRepository.deleteByGroupIdAndMemorialId(groupId, memorialId);
    }
}
