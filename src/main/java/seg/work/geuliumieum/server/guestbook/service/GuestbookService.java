package seg.work.geuliumieum.server.guestbook.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.GuestbookEntry;
import seg.work.geuliumieum.server.common.entity.Memorial;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.GuestbookEntryRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.config.security.UserRole;
import seg.work.geuliumieum.server.guestbook.dto.request.GuestbookRequest;
import seg.work.geuliumieum.server.guestbook.dto.response.GuestbookResponse;
import seg.work.geuliumieum.server.memorial.service.MemorialService;
import seg.work.geuliumieum.server.notification.event.NotificationEvent;

@Service
@RequiredArgsConstructor
public class GuestbookService {

    private final GuestbookEntryRepository guestbookEntryRepository;
    private final MemorialRepository memorialRepository;
    private final MemorialService memorialService;
    private final ApplicationEventPublisher eventPublisher;

    public Slice<GuestbookResponse> listByMemorial(UserInfo userInfo, Long memorialId, @ParameterObject Pageable pageable) {
        memorialService.checkAccess(userInfo, memorialId);
        return guestbookEntryRepository.findByMemorialIdAndIsApprovedTrue(memorialId, pageable).map(GuestbookResponse::from);
    }

    @Transactional
    public GuestbookResponse create(UserInfo userInfo, Long memorialId, GuestbookRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Memorial memorial = memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        GuestbookEntry guestbookEntry = new GuestbookEntry();
        guestbookEntry.setMemorialId(memorialId);
        guestbookEntry.setUserId(userInfo.getId());
        guestbookEntry.setAuthorName(request.getAuthorName());
        guestbookEntry.setContent(request.getContent());
        guestbookEntry.setIsApproved(Boolean.FALSE);
        guestbookEntryRepository.save(guestbookEntry);

        // 추모관 생성자에게 알림 발송 (승인 대기 알림)
        if (memorial.getCreatedBy() != null && !memorial.getCreatedBy().equals(userInfo.getId())) {
            eventPublisher.publishEvent(NotificationEvent.builder()
                .userId(memorial.getCreatedBy())
                .type("GUESTBOOK_WAITING")
                .title("방명록 승인 대기")
                .message(userInfo.getName() + "님이 방명록을 남겼습니다. 승인이 필요합니다.")
                .relatedType("MEMORIAL")
                .relatedId(memorialId)
                .build());
        }

        return GuestbookResponse.from(guestbookEntry);
    }

    @Transactional
    public void update(UserInfo userInfo, Long entryId, GuestbookRequest request) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        GuestbookEntry guestbookEntry = guestbookEntryRepository.findById(entryId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!userInfo.getId().equals(guestbookEntry.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (request.getAuthorName() != null) {
            guestbookEntry.setAuthorName(request.getAuthorName());
        }
        if (request.getContent() != null) {
            guestbookEntry.setContent(request.getContent());
        }
        // 수정 시 재승인 필요 정책은 추후 고려. 여기서는 승인 상태 유지.
        guestbookEntryRepository.save(guestbookEntry);
    }

    @Transactional
    public void delete(UserInfo userInfo, Long entryId) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        GuestbookEntry guestbookEntry = guestbookEntryRepository.findById(entryId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!userInfo.getId().equals(guestbookEntry.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        guestbookEntryRepository.delete(guestbookEntry);
    }

    @Transactional
    public void approve(UserInfo userInfo, Long entryId) {
        if (userInfo == null || userInfo.getRole() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!(userInfo.getRole() == UserRole.ADMIN || userInfo.getRole() == UserRole.SUPER_ADMIN)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        GuestbookEntry guestbookEntry = guestbookEntryRepository.findById(entryId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        guestbookEntry.setIsApproved(Boolean.TRUE);
        guestbookEntryRepository.save(guestbookEntry);
    }
}
