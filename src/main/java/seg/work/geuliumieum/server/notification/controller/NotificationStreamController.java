package seg.work.geuliumieum.server.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.notification.service.NotificationSseService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/notification", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
@Tag(name = "Notification", description = "알림 SSE 스트림")
public class NotificationStreamController {

    private final NotificationSseService sseService;

    @Operation(summary = "SSE 구독", description = "실시간 알림 스트림을 구독합니다.")
    @GetMapping("/stream")
    public SseEmitter stream(UserInfo userInfo) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return sseService.subscribe(userInfo.getId());
    }
}
