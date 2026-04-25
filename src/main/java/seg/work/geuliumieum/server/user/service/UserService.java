package seg.work.geuliumieum.server.user.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.entity.Memorial;
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
import seg.work.geuliumieum.server.upload.service.UploadService;
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
    private final UploadService uploadService;

    @Cacheable(cacheNames = "user:me", key = "#userId", unless = "#result == null")
    public UserMeResponse getCurrentUser(Long userId) {
        if (userId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        if (user.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.USER_DELETED);
        }
        return UserMeResponse.from(user);
    }

    @Cacheable(cacheNames = "user:profile", key = "#id", unless = "#result == null")
    public UserMeResponse getUserProfile(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        if (user.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.USER_DELETED);
        }
        return UserMeResponse.from(user);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "user:me", key = "#id"),
        @CacheEvict(cacheNames = "user:profile", key = "#id")
    })
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
        if (request.getMarketingAgreed() != null) {
            user.setMarketingAgreed(request.getMarketingAgreed());
        }
        userRepository.save(user);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "user:me", key = "#id"),
        @CacheEvict(cacheNames = "user:profile", key = "#id")
    })
    public void deleteUser(Long id, Long actorId) {
        if (actorId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!actorId.equals(id)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        if (user.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.ALREADY_WITHDRAWN);
        }

        // 소프트 딜리트 처리
        user.setIsActive(false);
        user.setDeletedAt(java.time.LocalDateTime.now());

        // 중복 가입 허용을 위한 이메일 변조 (Unique Constraint 우회)
        user.setEmail(user.getEmail() + "#" + java.util.UUID.randomUUID().toString().substring(0, 8));

        userRepository.save(user);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "user:me", key = "#id"),
        @CacheEvict(cacheNames = "user:profile", key = "#id")
    })
    public void updateProfilePhoto(Long id, Long actorId, ProfilePhotoUpdateRequest request) {
        if (actorId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!actorId.equals(id)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        if (request.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(uploadService.confirmFile(request.getProfilePhotoUrl()));
        }
        userRepository.save(user);
    }

    @Cacheable(cacheNames = "user:activity", key = "#id", unless = "#result == null")
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
        Page<MemorialMember> page = memorialMemberRepository.findByUserId(id, pageable);
        List<Long> memorialIds = page.getContent().stream()
            .map(MemorialMember::getMemorialId)
            .toList();

        if (memorialIds.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, false);
        }

        // In 쿼리로 한 번에 조회하여 N+1 방지
        List<Memorial> memorials = memorialRepository.findAllByIdIn(memorialIds);

        // 순서 보장을 위해 Map 활용 (필요시)
        Map<Long, Memorial> memorialMap = memorials.stream()
            .collect(Collectors.toMap(Memorial::getId, m -> m));

        List<MemorialResponse> content = memorialIds.stream()
            .map(memorialMap::get)
            .filter(Objects::nonNull)
            .map(MemorialResponse::from)
            .collect(Collectors.toList());

        return new SliceImpl<>(content, pageable, page.hasNext());
    }
}
