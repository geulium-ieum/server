package seg.work.geuliumieum.server.announcement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.announcement.dto.AnnouncementResponse;
import seg.work.geuliumieum.server.announcement.service.AnnouncementService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcement")
@Tag(name = "Announcement", description = "공지사항 공개 조회 API")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "공지사항 목록", description = "발행된 공지사항 목록을 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<Slice<AnnouncementResponse>> list(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(announcementService.list(pageable));
    }

    @Operation(summary = "고정된 공지사항", description = "상단에 고정된 발행 공지사항 목록을 조회합니다.")
    @GetMapping("/pinned")
    public ResponseEntity<Slice<AnnouncementResponse>> pinned(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(announcementService.pinned(pageable));
    }

    @Operation(summary = "공지사항 상세", description = "발행된 공지사항 상세를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.get(id));
    }
}
