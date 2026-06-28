package seg.work.geuliumieum.server.admin.service;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.admin.dto.response.AdminAnnouncementResponse;
import seg.work.geuliumieum.server.announcement.dto.request.AnnouncementCreateRequest;
import seg.work.geuliumieum.server.announcement.dto.request.AnnouncementUpdateRequest;
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

    private void ensureAdmin(UserInfo userInfo) {
        if (userInfo == null || userInfo.getRole() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!(userInfo.getRole() == UserRole.ADMIN || userInfo.getRole() == UserRole.SUPER_ADMIN)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

    @Transactional
    public AdminAnnouncementResponse create(UserInfo userInfo, AnnouncementCreateRequest request) {
        ensureAdmin(userInfo);
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setAuthorId(userInfo.getId());
        announcement.setIsPinned(Boolean.TRUE.equals(request.getIsPinned()));
        announcement.setIsPublished(false);
        announcement.setPublishedAt(null);
        announcementRepository.save(announcement);
        return AdminAnnouncementResponse.from(announcement);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "announcement:detail", key = "#id")
    })
    public void update(UserInfo userInfo, Long id, AnnouncementUpdateRequest request) {
        ensureAdmin(userInfo);
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
        @CacheEvict(cacheNames = "announcement:detail", key = "#id")
    })
    public void delete(UserInfo userInfo, Long id) {
        ensureAdmin(userInfo);
        Announcement announcement = announcementRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        announcementRepository.delete(announcement);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "announcement:detail", key = "#id")
    })
    public void publish(UserInfo userInfo, Long id) {
        ensureAdmin(userInfo);
        Announcement announcement = announcementRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        announcement.setIsPublished(true);
        announcement.setPublishedAt(OffsetDateTime.now());
        announcementRepository.save(announcement);
    }
}
