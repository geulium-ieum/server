package seg.work.geuliumieum.server.tribute.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.ApplicationEventPublisher;
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
import seg.work.geuliumieum.server.memorial.service.MemorialService;
import seg.work.geuliumieum.server.notification.event.NotificationEvent;
import seg.work.geuliumieum.server.tribute.dto.request.TributeRequest;
import seg.work.geuliumieum.server.tribute.dto.response.TributeResponse;

@Service
@RequiredArgsConstructor
public class TributeService {

    private final TributeRepository tributeRepository;
    private final MemorialRepository memorialRepository;
    private final MemorialService memorialService;
    private final ApplicationEventPublisher eventPublisher;

    public Slice<TributeResponse> listByMemorial(Long memorialId, @ParameterObject Pageable pageable, UserInfo user) {
        memorialService.checkAccess(user, memorialId);
        return tributeRepository.findByMemorialId(memorialId, pageable).map(TributeResponse::from);
    }

    @Transactional
    public TributeResponse create(Long memorialId, UserInfo user, TributeRequest request) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Memorial memorial = memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        Tribute t = new Tribute();
        t.setMemorialId(memorialId);
        t.setUserId(user.getId());
        t.setContent(request.getContent());
        t.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()));
        tributeRepository.save(t);

        // 추모관 생성자에게 알림 발송
        if (memorial.getCreatedBy() != null && !memorial.getCreatedBy().equals(user.getId())) {
            eventPublisher.publishEvent(NotificationEvent.builder()
                .userId(memorial.getCreatedBy())
                .type("TRIBUTE")
                .title("새로운 헌화")
                .message(user.getName() + "님이 " + memorial.getDeceasedName() + "님에게 헌화하셨습니다.")
                .relatedType("MEMORIAL")
                .relatedId(memorialId)
                .build());
        }

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
