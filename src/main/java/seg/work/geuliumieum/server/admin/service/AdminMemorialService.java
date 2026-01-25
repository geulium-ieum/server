package seg.work.geuliumieum.server.admin.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import seg.work.geuliumieum.server.admin.dto.response.MemorialWithStatsResponse;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.entity.MemorialWithStatsView;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.common.repository.MemorialWithStatsViewRepository;
import seg.work.geuliumieum.server.config.security.UserRole;
import seg.work.geuliumieum.server.memorial.constant.STATUS;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;

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

    private void ensureAdmin(UserInfo userInfo) {
        if (userInfo == null || userInfo.getRole() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!(userInfo.getRole() == UserRole.ADMIN || userInfo.getRole() == UserRole.SUPER_ADMIN)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

    public Slice<MemorialResponse> getPendingList(UserInfo userInfo, @ParameterObject Pageable pageable) {
        ensureAdmin(userInfo);
        return memorialRepository.findByStatus(STATUS.PENDING, pageable).map(MemorialResponse::from);
    }

    public Slice<MemorialResponse> getAll(UserInfo userInfo, @ParameterObject Pageable pageable) {
        ensureAdmin(userInfo);
        return memorialRepository.findAllBy(pageable).map(MemorialResponse::from);
    }

    public void approve(UserInfo userInfo, Long memorialId) {
        ensureAdmin(userInfo);
        Memorial memorial = memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        memorial.setStatus(STATUS.APPROVED);
        memorial.setApprovedBy(userInfo.getId());
        memorial.setApprovedAt(OffsetDateTime.now());
        memorial.setRejectionReason(null);
        memorialRepository.save(memorial);
    }

    public void reject(UserInfo userInfo, Long memorialId, String reason) {
        ensureAdmin(userInfo);
        Memorial memorial = memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        memorial.setStatus(STATUS.REJECT);
        memorial.setApprovedBy(userInfo.getId());
        memorial.setApprovedAt(OffsetDateTime.now());
        memorial.setRejectionReason(reason);
        memorialRepository.save(memorial);
    }
}
