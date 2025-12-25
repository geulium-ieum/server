package seg.work.geuliumieum.server.admin.controller;

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
import seg.work.geuliumieum.server.admin.service.AdminContentService;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.guestbook.dto.response.GuestbookResponse;
import seg.work.geuliumieum.server.tribute.dto.response.TributeResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/content")
@Tag(name = "Admin Content", description = "관리자 콘텐츠 모니터링 API")
public class AdminContentController {

    private final AdminContentService adminContentService;

    @Operation(summary = "모든 추모글 조회", description = "시스템 내 모든 추모글을 조회합니다(ADMIN|SUPER_ADMIN)")
    @GetMapping("/tribute")
    public ResponseEntity<Slice<TributeResponse>> listTributes(UserInfo admin, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(adminContentService.listAllTributes(admin, pageable));
    }

    @Operation(summary = "부적절 추모글 삭제", description = "특정 추모글을 삭제합니다(ADMIN|SUPER_ADMIN)")
    @DeleteMapping("/tribute/{id}")
    public ResponseEntity<Void> deleteTribute(UserInfo admin, @PathVariable("id") Long tributeId) {
        adminContentService.deleteTribute(admin, tributeId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모든 방명록 조회", description = "시스템 내 모든 방명록을 조회합니다(ADMIN|SUPER_ADMIN)")
    @GetMapping("/guestbook")
    public ResponseEntity<Slice<GuestbookResponse>> listGuestbooks(UserInfo admin, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(adminContentService.listAllGuestbooks(admin, pageable));
    }

    @Operation(summary = "방명록 승인", description = "특정 방명록을 승인합니다(ADMIN|SUPER_ADMIN)")
    @PatchMapping("/guestbook/{id}/approve")
    public ResponseEntity<Void> approveGuestbook(UserInfo admin, @PathVariable("id") Long entryId) {
        adminContentService.approveGuestbook(admin, entryId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "부적절 방명록 삭제", description = "특정 방명록을 삭제합니다(ADMIN|SUPER_ADMIN)")
    @DeleteMapping("/guestbook/{id}")
    public ResponseEntity<Void> deleteGuestbook(UserInfo admin, @PathVariable("id") Long entryId) {
        adminContentService.deleteGuestbook(admin, entryId);
        return ResponseEntity.ok().build();
    }
}
