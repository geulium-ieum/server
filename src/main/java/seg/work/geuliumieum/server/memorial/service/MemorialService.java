package seg.work.geuliumieum.server.memorial.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.FamilyGroupMemberRepository;
import seg.work.geuliumieum.server.common.repository.FamilyGroupMemorialRepository;
import seg.work.geuliumieum.server.common.repository.MemorialMemberRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.config.security.UserRole;
import seg.work.geuliumieum.server.memorial.constant.VISIBILITY;
import seg.work.geuliumieum.server.memorial.dto.request.RegisterRequest;
import seg.work.geuliumieum.server.memorial.dto.request.UpdateRequest;
import seg.work.geuliumieum.server.memorial.dto.response.AccessResponse;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;

@Slf4j
@Service
public class MemorialService {

    private final MemorialRepository memorialRepository;
    private final MemorialMemberRepository memorialMemberRepository;
    private final FamilyGroupMemberRepository familyGroupMemberRepository;
    private final FamilyGroupMemorialRepository familyGroupMemorialRepository;

    private MemorialService self;

    public MemorialService(MemorialRepository memorialRepository,
        MemorialMemberRepository memorialMemberRepository,
        FamilyGroupMemberRepository familyGroupMemberRepository,
        FamilyGroupMemorialRepository familyGroupMemorialRepository) {
        this.memorialRepository = memorialRepository;
        this.memorialMemberRepository = memorialMemberRepository;
        this.familyGroupMemberRepository = familyGroupMemberRepository;
        this.familyGroupMemorialRepository = familyGroupMemorialRepository;
    }

    @Autowired
    public void setSelf(@Lazy MemorialService self) {
        this.self = self;
    }

    public MemorialResponse getMemorial(Long id, UserInfo userInfo) {
        checkAccess(userInfo, id);
        return self.getMemorialDetail(id);
    }

    @Cacheable(cacheNames = "memorial:detail", key = "#id")
    public MemorialResponse getMemorialDetail(Long id) {
        Memorial memorial = memorialRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        return MemorialResponse.from(memorial);
    }

    public Slice<MemorialResponse> getMemorialList(Pageable pageable) {
        return memorialRepository.findAllBy(pageable).map(MemorialResponse::from);
    }

    @Transactional
    public void createMemorial(UserInfo userInfo, RegisterRequest request) {
        Memorial memorial = request.toEntity();
        memorialRepository.save(memorial);
    }

    @Transactional
    @CacheEvict(cacheNames = {"memorial:access", "memorial:detail"}, allEntries = true)
    public void updateMemorial(UserInfo userInfo, Long id, UpdateRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Memorial memorial = memorialRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (!Objects.equals(memorial.getCreatedBy(), userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        request.applyTo(memorial);
        memorialRepository.save(memorial);
    }

    @Transactional
    @CacheEvict(cacheNames = {"memorial:access", "memorial:detail"}, allEntries = true)
    public void deleteMemorial(UserInfo userInfo, Long id) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Memorial memorial = memorialRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (!Objects.equals(memorial.getCreatedBy(), userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        memorialRepository.delete(memorial);
    }

    public Slice<MemorialResponse> filter(String name, String birthFrom, String birthTo, String deathFrom, String deathTo, Pageable pageable) {
        Specification<Memorial> spec = (root, q, cb) -> cb.conjunction();

        LocalDate bf = parseDate(birthFrom);
        LocalDate bt = parseDate(birthTo);
        LocalDate df = parseDate(deathFrom);
        LocalDate dt = parseDate(deathTo);

        if (name != null) {
            spec = spec.and((root, q, cb) -> cb.like(root.get("deceasedName"), "%" + name + "%"));
        }
        if (bf != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("birthDate"), bf));
        }
        if (bt != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("birthDate"), bt));
        }
        if (df != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("deathDate"), df));
        }
        if (dt != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("deathDate"), dt));
        }

        Page<Memorial> page = memorialRepository.findAll(spec, pageable);
        return page.map(MemorialResponse::from);
    }

    public void checkAccess(UserInfo userInfo, Long id) {
        AccessResponse access = self.getAccess(userInfo, id);
        if (!access.isAllowed()) {
            if ("LOGIN_REQUIRED".equals(access.getReason())) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

    @Cacheable(cacheNames = "memorial:access", key = "#id + ':' + (#userInfo != null ? #userInfo.id : 'guest')")
    public AccessResponse getAccess(UserInfo userInfo, Long id) {
        Memorial memorial = memorialRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));

        // 1. 전체 공개
        if (memorial.getVisibility() == VISIBILITY.PUBLIC) {
            return AccessResponse.builder().allowed(true).reason("PUBLIC").build();
        }

        // 2. 비공개 또는 가족 공개인데 로그인하지 않은 경우
        if (userInfo == null || userInfo.getId() == null) {
            return AccessResponse.builder().allowed(false).reason("LOGIN_REQUIRED").build();
        }

        // 3. 관리자 권한 (전역 관리자)
        if (userInfo.getRole() == UserRole.ADMIN || userInfo.getRole() == UserRole.SUPER_ADMIN) {
            return AccessResponse.builder().allowed(true).reason("ADMIN").build();
        }

        // 4. 소유자 (생성자)
        if (Objects.equals(memorial.getCreatedBy(), userInfo.getId())) {
            return AccessResponse.builder().allowed(true).reason("OWNER").build();
        }

        // 5. 추모관 직접 멤버십 확인
        boolean isMemorialMember = memorialMemberRepository.existsByMemorialIdAndUserId(id, userInfo.getId());
        if (isMemorialMember) {
            return AccessResponse.builder().allowed(true).reason("MEMORIAL_MEMBER").build();
        }

        // 6. 가족 공개일 경우 가족 그룹 멤버십 확인
        if (memorial.getVisibility() == VISIBILITY.FAMILY_ONLY) {
            // 이 추모관이 연결된 모든 가족 그룹 ID 조회
            List<Long> groupIds = familyGroupMemorialRepository.findAllByMemorialId(id)
                .stream()
                .map(seg.work.geuliumieum.server.common.entity.FamilyGroupMemorial::getGroupId)
                .toList();

            if (!groupIds.isEmpty()) {
                // 사용자가 해당 그룹들 중 하나라도 멤버인지 확인
                boolean isFamilyMember = familyGroupMemberRepository.existsByUserIdAndGroupIdIn(userInfo.getId(), groupIds);
                if (isFamilyMember) {
                    return AccessResponse.builder().allowed(true).reason("FAMILY_MEMBER").build();
                }
            }
        }

        return AccessResponse.builder().allowed(false).reason("NO_PERMISSION").build();
    }

    private LocalDate parseDate(String value) {
        try {
            return (value == null || value.isBlank()) ? null : LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
