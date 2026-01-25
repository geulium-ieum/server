package seg.work.geuliumieum.server.tribute.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(cacheNames = "tribute:list", key = "#memorialId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Slice<TributeResponse> listByMemorial(Long memorialId, @ParameterObject Pageable pageable, UserInfo userInfo) {
        memorialService.checkAccess(userInfo, memorialId);
        return tributeRepository.findByMemorialId(memorialId, pageable).map(TributeResponse::from);
    }

    @Transactional
    @CacheEvict(cacheNames = "tribute:list", key = "#memorialId")
    public TributeResponse create(Long memorialId, UserInfo userInfo, TributeRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Memorial memorial = memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        Tribute tribute = new Tribute();
        tribute.setMemorialId(memorialId);
        tribute.setUserId(userInfo.getId());
        tribute.setContent(request.getContent());
        tribute.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()));
        tributeRepository.save(tribute);

        // 추모관 생성자에게 알림 발송
        if (memorial.getCreatedBy() != null && !memorial.getCreatedBy().equals(userInfo.getId())) {
            eventPublisher.publishEvent(NotificationEvent.builder()
                .userId(memorial.getCreatedBy())
                .type("TRIBUTE")
                .title("새로운 헌화")
                .message(userInfo.getName() + "님이 " + memorial.getDeceasedName() + "님에게 헌화하셨습니다.")
                .relatedType("MEMORIAL")
                .relatedId(memorialId)
                .build());
        }

        return TributeResponse.from(tribute);
    }

    @Transactional
    @CacheEvict(cacheNames = "tribute:list", allEntries = true)
    public void update(Long tributeId, UserInfo userInfo, TributeRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Tribute tribute = tributeRepository.findById(tributeId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!userInfo.getId().equals(tribute.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (request.getContent() != null) {
            tribute.setContent(request.getContent());
        }
        if (request.getIsPublic() != null) {
            tribute.setIsPublic(request.getIsPublic());
        }
        tributeRepository.save(tribute);
    }

    @Transactional
    @CacheEvict(cacheNames = "tribute:list", allEntries = true)
    public void delete(Long tributeId, UserInfo userInfo) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Tribute tribute = tributeRepository.findById(tributeId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!userInfo.getId().equals(tribute.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        tributeRepository.delete(tribute);
    }

    public Slice<TributeResponse> listByUser(Long userId, @ParameterObject Pageable pageable) {
        return tributeRepository.findByUserId(userId, pageable).map(TributeResponse::from);
    }
}
