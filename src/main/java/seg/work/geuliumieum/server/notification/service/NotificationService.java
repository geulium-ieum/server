package seg.work.geuliumieum.server.notification.service;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.entity.Notification;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.repository.NotificationRepository;
import seg.work.geuliumieum.server.notification.dto.NotificationResponse;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSseService sseService;

    public Slice<NotificationResponse> list(UserInfo userInfo, @ParameterObject Pageable pageable) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userInfo.getId(), pageable).map(NotificationResponse::from);
    }

    public long unreadCount(UserInfo userInfo) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return notificationRepository.countByUserIdAndIsReadFalse(userInfo.getId());
    }

    @Transactional
    public void markRead(UserInfo userInfo, Long id) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Notification n = notificationRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!userInfo.getId().equals(n.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (Boolean.FALSE.equals(n.getIsRead()) || n.getIsRead() == null) {
            n.setIsRead(true);
            notificationRepository.save(n);
            sseService.sendRead(userInfo.getId(), n.getId());
        }
    }

    @Transactional
    public void markAllRead(UserInfo userInfo) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        notificationRepository.markAllRead(userInfo.getId());
        // optional: we could emit a summary event; keep as individual events on client refresh
    }

    @Transactional
    public void deleteOne(UserInfo userInfo, Long id) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!userInfo.getId().equals(notification.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        notificationRepository.deleteByUserIdAndId(userInfo.getId(), id);
    }

    @Transactional
    public void deleteAll(UserInfo userInfo) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        notificationRepository.deleteByUserId(userInfo.getId());
    }

    // For internal publish from other modules
    @Transactional
    public void publish(Long userId, String type, String title, String message,
        String relatedType, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setIsRead(false);
        // createdAt will be set by auditing
        notificationRepository.save(notification);
        NotificationResponse resp = NotificationResponse.from(notification);
        sseService.sendNew(userId, resp);
    }
}
