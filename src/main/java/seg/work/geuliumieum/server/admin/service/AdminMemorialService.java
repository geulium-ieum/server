package seg.work.geuliumieum.server.admin.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seg.work.geuliumieum.server.admin.dto.MemorialWithStatsResponse;
import seg.work.geuliumieum.server.admin.entity.MemorialWithStatsView;
import seg.work.geuliumieum.server.admin.repository.MemorialWithStatsViewRepository;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class AdminMemorialService {

    private final MemorialWithStatsViewRepository memorialWithStatsViewRepository;

    public MemorialWithStatsResponse getMemorialWithStats(long memorialId) {
        Optional<MemorialWithStatsView> opt = memorialWithStatsViewRepository.findByMemorialId(memorialId);
        MemorialWithStatsView v = opt.orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        return MemorialWithStatsResponse.toResponse(v);
    }
}
