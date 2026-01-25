package seg.work.geuliumieum.server.memorial.service;

import java.time.LocalDate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.memorial.dto.request.RegisterRequest;
import seg.work.geuliumieum.server.memorial.dto.request.UpdateRequest;
import seg.work.geuliumieum.server.memorial.dto.response.AccessResponse;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;
import seg.work.geuliumieum.server.upload.service.UploadService;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemorialService {

    private final MemorialAccessService memorialAccessService;

    private final MemorialRepository memorialRepository;

    private final UploadService uploadService;

    @Cacheable(cacheNames = "memorial:detail", key = "#id")
    public MemorialResponse getMemorial(UserInfo userInfo, Long id) {
        checkAccess(userInfo, id);
        Memorial memorial = memorialRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        return MemorialResponse.from(memorial);
    }

    public Slice<MemorialResponse> getMemorialList(Pageable pageable) {
        return memorialRepository.findAllBy(pageable).map(MemorialResponse::from);
    }

    @Transactional
    public void createMemorial(RegisterRequest request) {
        if (request.getPhotoUrl() != null) {
            request.setPhotoUrl(uploadService.confirmFile(request.getPhotoUrl()));
        }
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
        if (request.getPhotoUrl() != null) {
            request.setPhotoUrl(uploadService.confirmFile(request.getPhotoUrl()));
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

    public Slice<MemorialResponse> filter(String name, String birthDate, String deathDate, Pageable pageable) {
        Specification<Memorial> spec = (root, q, cb) -> cb.conjunction();

        LocalDate bd = parseDate(birthDate);
        LocalDate dd = parseDate(deathDate);

        if (name != null) {
            spec = spec.and((root, q, cb) -> cb.like(root.get("deceasedName"), "%" + name + "%"));
        }
        if (bd != null && dd != null) {
            spec = spec.and((root, q, cb) -> cb.between(root.get("birthDate"), bd, dd));
        }

        Page<Memorial> page = memorialRepository.findAll(spec, pageable);
        return page.map(MemorialResponse::from);
    }

    public void checkAccess(UserInfo userInfo, Long id) {
        AccessResponse access = memorialAccessService.getAccess(userInfo, id);
        if (!access.isAllowed()) {
            if ("LOGIN_REQUIRED".equals(access.getReason())) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

    public AccessResponse getAccess(UserInfo userInfo, Long id) {
        return memorialAccessService.getAccess(userInfo, id);
    }

    private LocalDate parseDate(String value) {
        try {
            return (value == null || value.isBlank()) ? null : LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
