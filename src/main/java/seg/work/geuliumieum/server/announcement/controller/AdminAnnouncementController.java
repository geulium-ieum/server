package seg.work.geuliumieum.server.announcement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.announcement.dto.AnnouncementCreateRequest;
import seg.work.geuliumieum.server.announcement.dto.AnnouncementResponse;
import seg.work.geuliumieum.server.announcement.dto.AnnouncementUpdateRequest;
import seg.work.geuliumieum.server.announcement.service.AdminAnnouncementService;
import seg.work.geuliumieum.server.common.dto.UserInfo;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/announcement")
@Tag(name = "Admin: Announcement", description = "공지사항 관리 API (ADMIN|SUPER_ADMIN)")
public class AdminAnnouncementController {

    private final AdminAnnouncementService adminAnnouncementService;

    @Operation(summary = "공지사항 작성", description = "관리자가 공지사항을 작성합니다.")
    @PostMapping
    public ResponseEntity<AnnouncementResponse> create(UserInfo user,
        @Valid @RequestBody AnnouncementCreateRequest request) {
        return ResponseEntity.ok(adminAnnouncementService.create(user, request));
    }

    @Operation(summary = "공지사항 수정", description = "관리자가 공지사항을 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(UserInfo user,
        @PathVariable("id") Long id,
        @Valid @RequestBody AnnouncementUpdateRequest request) {
        adminAnnouncementService.update(user, id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "공지사항 삭제", description = "관리자가 공지사항을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(UserInfo user, @PathVariable("id") Long id) {
        adminAnnouncementService.delete(user, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "공지사항 발행", description = "관리자가 공지사항을 발행합니다.")
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publish(UserInfo user, @PathVariable("id") Long id) {
        adminAnnouncementService.publish(user, id);
        return ResponseEntity.ok().build();
    }
}
