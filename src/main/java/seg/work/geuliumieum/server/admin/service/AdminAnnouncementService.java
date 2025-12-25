package seg.work.geuliumieum.server.admin.service;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.admin.dto.response.AdminAnnouncementResponse;
import seg.work.geuliumieum.server.announcement.dto.AnnouncementCreateRequest;
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
    public AdminAnnouncementResponse create(UserInfo user, AnnouncementCreateRequest request) {
        ensureAdmin(user);
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setAuthorId(user.getId());
        announcement.setIsPinned(Boolean.TRUE.equals(request.getIsPinned()));
        announcement.setIsPublished(false);
        announcement.setPublishedAt(null);
        announcementRepository.save(announcement);
        return AdminAnnouncementResponse.from(announcement);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "announcement:list", allEntries = true),
        @CacheEvict(cacheNames = "announcement:detail", key = "#id")
    })
    public void update(UserInfo user, Long id, AnnouncementUpdateRequest request) {
        ensureAdmin(user);
        Announcement announcement = announcementRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (request.getTitle() != null) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            announcement.setContent(request.getContent());
        }
        if (request.getIsPinned() != null) {
            announcement.setIsPinned(request.getIsPinned());
        }
        announcementRepository.save(announcement);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "announcement:list", allEntries = true),
        @CacheEvict(cacheNames = "announcement:detail", key = "#id")
    })
    public void delete(UserInfo user, Long id) {
        ensureAdmin(user);
        Announcement announcement = announcementRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        announcementRepository.delete(announcement);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "announcement:list", allEntries = true),
        @CacheEvict(cacheNames = "announcement:detail", key = "#id")
    })
    public void publish(UserInfo user, Long id) {
        ensureAdmin(user);
        Announcement announcement = announcementRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        announcement.setIsPublished(true);
        announcement.setPublishedAt(OffsetDateTime.now());
        announcementRepository.save(announcement);
    }
}
