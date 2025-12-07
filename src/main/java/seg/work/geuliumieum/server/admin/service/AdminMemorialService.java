package seg.work.geuliumieum.server.admin.service;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import seg.work.geuliumieum.server.admin.dto.response.MemorialWithStatsResponse;
import seg.work.geuliumieum.server.common.entity.MemorialWithStatsView;
import seg.work.geuliumieum.server.common.repository.MemorialWithStatsViewRepository;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.config.security.UserRole;
import seg.work.geuliumieum.server.memorial.constant.STATUS;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMemorialService {

    private final MemorialWithStatsViewRepository memorialWithStatsViewRepository;
    private final MemorialRepository memorialRepository;

    public MemorialWithStatsResponse getMemorialWithStats(long memorialId) {
        Optional<MemorialWithStatsView> opt = memorialWithStatsViewRepository.findByMemorialId(memorialId);
        MemorialWithStatsView v = opt.orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        return MemorialWithStatsResponse.toResponse(v);
    }

    private void ensureAdmin(UserInfo user) {
        if (user == null || user.getRole() == null) throw new ApiException(ErrorCode.UNAUTHORIZED);
        if (!(user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.SUPER_ADMIN)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

    public Slice<MemorialResponse> getPendingList(UserInfo user, @ParameterObject Pageable pageable) {
        ensureAdmin(user);
        return memorialRepository.findByStatus(STATUS.PENDING, pageable).map(MemorialResponse::from);
    }

    public Slice<MemorialResponse> getAll(UserInfo user, @ParameterObject Pageable pageable) {
        ensureAdmin(user);
        return memorialRepository.findAllBy(pageable).map(MemorialResponse::from);
    }

    public void approve(UserInfo user, Long memorialId) {
        ensureAdmin(user);
        Memorial m = memorialRepository.findById(memorialId)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        m.setStatus(STATUS.APPROVED);
        m.setApprovedBy(user.getId());
        m.setApprovedAt(OffsetDateTime.now());
        m.setRejectionReason(null);
        memorialRepository.save(m);
    }

    public void reject(UserInfo user, Long memorialId, String reason) {
        ensureAdmin(user);
        Memorial m = memorialRepository.findById(memorialId)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        m.setStatus(STATUS.REJECT);
        m.setApprovedBy(user.getId());
        m.setApprovedAt(OffsetDateTime.now());
        m.setRejectionReason(reason);
        memorialRepository.save(m);
    }
}
