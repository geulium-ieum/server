package seg.work.geuliumieum.server.reminder.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.reminder.dto.request.ReminderRequest;
import seg.work.geuliumieum.server.reminder.dto.request.ReminderUpdateRequest;
import seg.work.geuliumieum.server.reminder.dto.response.ReminderResponse;
import seg.work.geuliumieum.server.reminder.service.ReminderService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reminder")
@Tag(name = "Reminder", description = "기일 알림 API")
public class ReminderController {

    private final ReminderService reminderService;

    @Operation(summary = "내 알림 목록", description = "현재 사용자의 기일 알림 목록을 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<Slice<ReminderResponse>> myReminders(UserInfo userInfo, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(reminderService.myReminders(userInfo, pageable));
    }

    @Operation(summary = "특정 추모관 알림 목록", description = "특정 추모관에 대한 나의 알림 목록을 조회합니다.")
    @GetMapping("/memorial/{id}/list")
    public ResponseEntity<Slice<ReminderResponse>> memorialReminders(UserInfo userInfo, @PathVariable("id") Long memorialId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(reminderService.memorialReminders(userInfo, memorialId, pageable));
    }

    @Operation(summary = "알림 생성", description = "특정 추모관에 대한 기일 알림을 생성합니다.")
    @PostMapping("/memorial/{id}")
    public ResponseEntity<ReminderResponse> create(UserInfo userInfo, @PathVariable("id") Long memorialId, @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(reminderService.create(userInfo, memorialId, request));
    }

    @Operation(summary = "알림 수정", description = "기일 알림을 수정합니다(소유자만)")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(UserInfo userInfo, @PathVariable("id") Long reminderId, @Valid @RequestBody ReminderUpdateRequest request) {
        reminderService.update(userInfo, reminderId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "알림 삭제", description = "기일 알림을 삭제합니다(소유자만)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(UserInfo userInfo, @PathVariable("id") Long reminderId) {
        reminderService.delete(userInfo, reminderId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "다가오는 알림(30일)", description = "앞으로 30일 이내 발생하는 나의 알림을 조회합니다.")
    @GetMapping("/upcoming")
    public ResponseEntity<List<ReminderResponse>> upcoming(UserInfo userInfo) {
        return ResponseEntity.ok(reminderService.upcoming(userInfo));
    }
}
