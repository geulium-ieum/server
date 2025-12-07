package seg.work.geuliumieum.server.user.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.entity.MemorialMember;
import seg.work.geuliumieum.server.common.entity.User;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.GuestbookEntryRepository;
import seg.work.geuliumieum.server.common.repository.MemorialMemberRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.common.repository.OfferingRepository;
import seg.work.geuliumieum.server.common.repository.TributeRepository;
import seg.work.geuliumieum.server.common.repository.UserRepository;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;
import seg.work.geuliumieum.server.user.dto.request.ProfilePhotoUpdateRequest;
import seg.work.geuliumieum.server.user.dto.request.UserUpdateRequest;
import seg.work.geuliumieum.server.user.dto.response.UserActivityResponse;
import seg.work.geuliumieum.server.user.dto.response.UserMeResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MemorialRepository memorialRepository;
    private final MemorialMemberRepository memorialMemberRepository;
    private final TributeRepository tributeRepository;
    private final OfferingRepository offeringRepository;
    private final GuestbookEntryRepository guestbookEntryRepository;

    @Cacheable(cacheNames = "user:me", key = "#userId", unless = "#result == null")
    public UserMeResponse getCurrentUser(Long userId) {
        if (userId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return UserMeResponse.from(user);
    }

    public UserMeResponse getUserProfile(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return UserMeResponse.from(user);
    }

    @Transactional
    public void updateUser(Long id, Long actorId, UserUpdateRequest request) {
        if (actorId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!actorId.equals(id)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id, Long actorId) {
        if (actorId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!actorId.equals(id)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    @Transactional
    public void updateProfilePhoto(Long id, Long actorId, ProfilePhotoUpdateRequest request) {
        if (actorId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!actorId.equals(id)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        userRepository.save(user);
    }

    public UserActivityResponse getUserActivity(Long id) {
        // 간단 합산 통계
        long tribute = tributeRepository.countByUserId(id);
        long offering = offeringRepository.countByUserId(id);
        long guestbook = guestbookEntryRepository.countByUserId(id);
        return new UserActivityResponse(tribute, offering, guestbook);
    }

    public Slice<MemorialResponse> getCreatedMemorials(Long id, @ParameterObject Pageable pageable) {
        return memorialRepository.findByCreatedBy(id, pageable).map(MemorialResponse::from);
    }

    public Slice<MemorialResponse> getJoinedMemorials(Long id, @ParameterObject Pageable pageable) {
        var page = memorialMemberRepository.findByUserId(id, pageable);
        List<Long> memorialIds = page.getContent().stream()
            .map(MemorialMember::getMemorialId)
            .toList();
        List<MemorialResponse> content = memorialIds.stream()
            .map(memorialId -> memorialRepository.findById(memorialId).orElse(null))
            .filter(m -> m != null)
            .map(MemorialResponse::from)
            .collect(Collectors.toList());
        return new SliceImpl<>(content, pageable, page.hasNext());
    }
}
