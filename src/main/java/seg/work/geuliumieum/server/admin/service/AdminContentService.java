package seg.work.geuliumieum.server.admin.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.GuestbookEntry;
import seg.work.geuliumieum.server.common.entity.Tribute;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.GuestbookEntryRepository;
import seg.work.geuliumieum.server.common.repository.TributeRepository;
import seg.work.geuliumieum.server.config.security.UserRole;
import seg.work.geuliumieum.server.guestbook.dto.response.GuestbookResponse;
import seg.work.geuliumieum.server.tribute.dto.response.TributeResponse;

@Service
@RequiredArgsConstructor
public class AdminContentService {

    private final TributeRepository tributeRepository;
    private final GuestbookEntryRepository guestbookEntryRepository;

    private void ensureAdmin(UserInfo user) {
        if (user == null || user.getRole() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!(user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.SUPER_ADMIN)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

    public Slice<TributeResponse> listAllTributes(UserInfo admin, @ParameterObject Pageable pageable) {
        ensureAdmin(admin);
        return tributeRepository.findAll(pageable).map(TributeResponse::from);
    }

    @Transactional
    public void deleteTribute(UserInfo admin, Long tributeId) {
        ensureAdmin(admin);
        Tribute tribute = tributeRepository.findById(tributeId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        tributeRepository.delete(tribute);
    }

    public Slice<GuestbookResponse> listAllGuestbooks(UserInfo admin, @ParameterObject Pageable pageable) {
        ensureAdmin(admin);
        return guestbookEntryRepository.findAll(pageable).map(GuestbookResponse::from);
    }

    @Transactional
    public void approveGuestbook(UserInfo admin, Long entryId) {
        ensureAdmin(admin);
        GuestbookEntry guestbookEntry = guestbookEntryRepository.findById(entryId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        guestbookEntry.setIsApproved(Boolean.TRUE);
        guestbookEntryRepository.save(guestbookEntry);
    }

    @Transactional
    public void deleteGuestbook(UserInfo admin, Long entryId) {
        ensureAdmin(admin);
        GuestbookEntry guestbookEntry = guestbookEntryRepository.findById(entryId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        guestbookEntryRepository.delete(guestbookEntry);
    }
}
