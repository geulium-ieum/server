package seg.work.geuliumieum.server.guestbook.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.GuestbookEntry;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.GuestbookEntryRepository;
import seg.work.geuliumieum.server.common.repository.MemorialRepository;
import seg.work.geuliumieum.server.config.security.UserRole;
import seg.work.geuliumieum.server.guestbook.dto.request.GuestbookRequest;
import seg.work.geuliumieum.server.guestbook.dto.response.GuestbookResponse;

@Service
@RequiredArgsConstructor
public class GuestbookService {

    private final GuestbookEntryRepository guestbookEntryRepository;
    private final MemorialRepository memorialRepository;

    public Slice<GuestbookResponse> listByMemorial(Long memorialId, @ParameterObject Pageable pageable) {
        // 존재 확인
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        return guestbookEntryRepository.findByMemorialIdAndIsApprovedTrue(memorialId, pageable)
            .map(GuestbookResponse::from);
    }

    @Transactional
    public GuestbookResponse create(Long memorialId, UserInfo user, GuestbookRequest request) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        memorialRepository.findById(memorialId).orElseThrow(() -> new ApiException(ErrorCode.MEMORIAL_NOT_FOUND));
        GuestbookEntry e = new GuestbookEntry();
        e.setMemorialId(memorialId);
        e.setUserId(user.getId());
        e.setAuthorName(request.getAuthorName());
        e.setContent(request.getContent());
        e.setIsApproved(Boolean.FALSE);
        guestbookEntryRepository.save(e);
        return GuestbookResponse.from(e);
    }

    @Transactional
    public void update(Long entryId, UserInfo user, GuestbookRequest request) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        GuestbookEntry e = guestbookEntryRepository.findById(entryId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(e.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (request.getAuthorName() != null) {
            e.setAuthorName(request.getAuthorName());
        }
        if (request.getContent() != null) {
            e.setContent(request.getContent());
        }
        // 수정 시 재승인 필요 정책은 추후 고려. 여기서는 승인 상태 유지.
        guestbookEntryRepository.save(e);
    }

    @Transactional
    public void delete(Long entryId, UserInfo user) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        GuestbookEntry e = guestbookEntryRepository.findById(entryId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(e.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        guestbookEntryRepository.delete(e);
    }

    @Transactional
    public void approve(Long entryId, UserInfo user) {
        if (user == null || user.getRole() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!(user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.SUPER_ADMIN)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        GuestbookEntry e = guestbookEntryRepository.findById(entryId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        e.setIsApproved(Boolean.TRUE);
        guestbookEntryRepository.save(e);
    }
}
