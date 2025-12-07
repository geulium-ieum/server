package seg.work.geuliumieum.server.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.notification.dto.NotificationResponse;
import seg.work.geuliumieum.server.notification.service.NotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록", description = "현재 사용자의 알림 목록을 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<Slice<NotificationResponse>> list(UserInfo user,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(notificationService.list(user, pageable));
    }

    @Operation(summary = "읽지 않은 알림 개수", description = "읽지 않은 알림 개수를 조회합니다.")
    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(UserInfo user) {
        return ResponseEntity.ok(notificationService.unreadCount(user));
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable("id") Long id, UserInfo user) {
        notificationService.markRead(user, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "현재 사용자의 모든 알림을 읽음 처리합니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> readAll(UserInfo user) {
        notificationService.markAllRead(user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOne(@PathVariable("id") Long id, UserInfo user) {
        notificationService.deleteOne(user, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모든 알림 삭제", description = "현재 사용자의 모든 알림을 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<Void> deleteAll(UserInfo user) {
        notificationService.deleteAll(user);
        return ResponseEntity.ok().build();
    }
}
