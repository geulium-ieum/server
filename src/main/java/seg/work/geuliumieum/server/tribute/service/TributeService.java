package seg.work.geuliumieum.server.tribute.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.entity.Tribute;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.common.repository.TributeRepository;
import seg.work.geuliumieum.server.memorial.constant.VISIBILITY;
import seg.work.geuliumieum.server.tribute.dto.request.TributeRequest;
import seg.work.geuliumieum.server.tribute.dto.response.TributeResponse;

@Service
@RequiredArgsConstructor
public class TributeService {

    private final TributeRepository tributeRepository;
    private final MemorialRepository memorialRepository;

    public Slice<TributeResponse> listByMemorial(Long memorialId, @ParameterObject Pageable pageable, UserInfo user) {
        Memorial memorial = memorialRepository.findById(memorialId)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        // 비공개/가족공개 등의 상세 접근정책은 이후 단계에서 강화
        if (memorial.getVisibility() != VISIBILITY.PUBLIC && (user == null || user.getId() == null)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return tributeRepository.findByMemorialId(memorialId, pageable).map(TributeResponse::from);
    }

    @Transactional
    public TributeResponse create(Long memorialId, UserInfo user, TributeRequest request) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        Tribute t = new Tribute();
        t.setMemorialId(memorialId);
        t.setUserId(user.getId());
        t.setContent(request.getContent());
        t.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()));
        tributeRepository.save(t);
        return TributeResponse.from(t);
    }

    @Transactional
    public void update(Long tributeId, UserInfo user, TributeRequest request) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Tribute t = tributeRepository.findById(tributeId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(t.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (request.getContent() != null) {
            t.setContent(request.getContent());
        }
        if (request.getIsPublic() != null) {
            t.setIsPublic(request.getIsPublic());
        }
        tributeRepository.save(t);
    }

    @Transactional
    public void delete(Long tributeId, UserInfo user) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Tribute t = tributeRepository.findById(tributeId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(t.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        tributeRepository.delete(t);
    }

    public Slice<TributeResponse> listByUser(Long userId, @ParameterObject Pageable pageable) {
        return tributeRepository.findByUserId(userId, pageable).map(TributeResponse::from);
    }
}
