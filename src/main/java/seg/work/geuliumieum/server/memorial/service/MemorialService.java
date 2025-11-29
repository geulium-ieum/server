package seg.work.geuliumieum.server.memorial.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.memorial.dto.request.RegisterRequest;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;

@Service
@RequiredArgsConstructor
public class MemorialService {

    private final MemorialRepository memorialRepository;

    public MemorialResponse getMemorial(Long id) {
        Memorial memorial = memorialRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        return MemorialResponse.from(memorial);
    }

    public Slice<MemorialResponse> getMemorialList(Pageable pageable) {
        return memorialRepository.findAllBy(pageable).map(MemorialResponse::from);
    }

    public void createMemorial(UserInfo userInfo, RegisterRequest request) {
        Memorial memorial = request.toEntity();
        memorialRepository.save(memorial);
    }
}
