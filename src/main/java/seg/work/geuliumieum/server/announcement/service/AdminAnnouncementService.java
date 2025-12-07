package seg.work.geuliumieum.server.announcement.service;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.announcement.dto.AnnouncementCreateRequest;
import seg.work.geuliumieum.server.announcement.dto.AnnouncementResponse;
import seg.work.geuliumieum.server.announcement.dto.AnnouncementUpdateRequest;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Announcement;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.AnnouncementRepository;
import seg.work.geuliumieum.server.config.security.UserRole;

@Service
@RequiredArgsConstructor
public class AdminAnnouncementService {

    private final AnnouncementRepository announcementRepository;

    private void ensureAdmin(UserInfo user) {
        if (user == null || user.getRole() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!(user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.SUPER_ADMIN)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

    @Transactional
    public AnnouncementResponse create(UserInfo user, AnnouncementCreateRequest request) {
        ensureAdmin(user);
        Announcement a = new Announcement();
        a.setTitle(request.getTitle());
        a.setContent(request.getContent());
        a.setAuthorId(user.getId());
        a.setIsPinned(Boolean.TRUE.equals(request.getIsPinned()));
        a.setIsPublished(false);
        a.setPublishedAt(null);
        announcementRepository.save(a);
        return AnnouncementResponse.from(a);
    }

    @Transactional
    public void update(UserInfo user, Long id, AnnouncementUpdateRequest request) {
        ensureAdmin(user);
        Announcement a = announcementRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (request.getTitle() != null) {
            a.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            a.setContent(request.getContent());
        }
        if (request.getIsPinned() != null) {
            a.setIsPinned(request.getIsPinned());
        }
        announcementRepository.save(a);
    }

    @Transactional
    public void delete(UserInfo user, Long id) {
        ensureAdmin(user);
        Announcement a = announcementRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        announcementRepository.delete(a);
    }

    @Transactional
    public void publish(UserInfo user, Long id) {
        ensureAdmin(user);
        Announcement a = announcementRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        a.setIsPublished(true);
        a.setPublishedAt(OffsetDateTime.now());
        announcementRepository.save(a);
    }
}
