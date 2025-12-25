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

    public Slice<NotificationResponse> list(UserInfo user, @ParameterObject Pageable pageable) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable).map(NotificationResponse::from);
    }

    public long unreadCount(UserInfo user) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markRead(UserInfo user, Long id) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Notification n = notificationRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(n.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (Boolean.FALSE.equals(n.getIsRead()) || n.getIsRead() == null) {
            n.setIsRead(true);
            notificationRepository.save(n);
            sseService.sendRead(user.getId(), n.getId());
        }
    }

    @Transactional
    public void markAllRead(UserInfo user) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        notificationRepository.markAllRead(user.getId());
        // optional: we could emit a summary event; keep as individual events on client refresh
    }

    @Transactional
    public void deleteOne(UserInfo user, Long id) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!user.getId().equals(notification.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        notificationRepository.deleteByUserIdAndId(user.getId(), id);
    }

    @Transactional
    public void deleteAll(UserInfo user) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        notificationRepository.deleteByUserId(user.getId());
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
