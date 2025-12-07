package seg.work.geuliumieum.server.memorial.service;

import java.time.LocalDate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import seg.work.geuliumieum.server.common.repository.MemorialMemberRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.memorial.constant.VISIBILITY;
import seg.work.geuliumieum.server.memorial.dto.request.RegisterRequest;
import seg.work.geuliumieum.server.memorial.dto.request.UpdateRequest;
import seg.work.geuliumieum.server.memorial.dto.response.AccessResponse;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemorialService {

    private final MemorialRepository memorialRepository;
    private final MemorialMemberRepository memorialMemberRepository;

    public MemorialResponse getMemorial(Long id) {
        Memorial memorial = memorialRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
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
    public void updateMemorial(UserInfo userInfo, Long id, UpdateRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Memorial memorial = memorialRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (!Objects.equals(memorial.getCreatedBy(), userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        request.applyTo(memorial);
        memorialRepository.save(memorial);
    }

    @Transactional
    public void deleteMemorial(UserInfo userInfo, Long id) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Memorial memorial = memorialRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        if (!Objects.equals(memorial.getCreatedBy(), userInfo.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        memorialRepository.delete(memorial);
    }

    public Slice<MemorialResponse> searchByDeceasedName(String name, Pageable pageable) {
        var slice = memorialRepository.findByDeceasedNameContainingIgnoreCase(
            name == null ? "" : name, pageable);
        return slice.map(MemorialResponse::from);
    }

    public Slice<MemorialResponse> filter(String birthFrom, String birthTo,
        String deathFrom, String deathTo,
        String location,
        Pageable pageable) {
        Specification<Memorial> spec = (root, q, cb) -> cb.conjunction();

        LocalDate bf = parseDate(birthFrom);
        LocalDate bt = parseDate(birthTo);
        LocalDate df = parseDate(deathFrom);
        LocalDate dt = parseDate(deathTo);

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
        if (location != null && !location.isBlank()) {
            String like = "%" + location.toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("location")), like));
        }

        Page<Memorial> page = memorialRepository.findAll(spec, pageable);
        return page.map(MemorialResponse::from);
    }

    public AccessResponse getAccess(UserInfo userInfo, Long id) {
        Memorial memorial = memorialRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));

        // 공개
        if (memorial.getVisibility() == VISIBILITY.PUBLIC) {
            return AccessResponse.builder().allowed(true).reason("PUBLIC").build();
        }
        // 로그인 필요
        if (userInfo == null || userInfo.getId() == null) {
            return AccessResponse.builder().allowed(false).reason("LOGIN_REQUIRED").build();
        }
        // 소유자
        if (Objects.equals(memorial.getCreatedBy(), userInfo.getId())) {
            return AccessResponse.builder().allowed(true).reason("OWNER").build();
        }
        // 멤버십
        boolean member = memorialMemberRepository.existsByMemorialIdAndUserId(id, userInfo.getId());
        if (member) {
            return AccessResponse.builder().allowed(true).reason("MEMBER").build();
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
