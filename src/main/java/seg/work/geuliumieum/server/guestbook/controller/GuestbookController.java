package seg.work.geuliumieum.server.guestbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.guestbook.dto.request.GuestbookRequest;
import seg.work.geuliumieum.server.guestbook.dto.response.GuestbookResponse;
import seg.work.geuliumieum.server.guestbook.service.GuestbookService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guestbook")
@Tag(name = "Guestbook", description = "방명록 API")
public class GuestbookController {

    private final GuestbookService guestbookService;

    @Operation(summary = "방명록 목록", description = "추모관별 승인된 방명록 목록을 조회합니다.")
    @GetMapping("/memorial/{id}/list")
    public ResponseEntity<Slice<GuestbookResponse>> list(@PathVariable("id") Long memorialId,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(guestbookService.listByMemorial(memorialId, pageable));
    }

    @Operation(summary = "방명록 작성", description = "해당 추모관에 방명록을 작성합니다.")
    @PostMapping("/memorial/{id}")
    public ResponseEntity<GuestbookResponse> create(@PathVariable("id") Long memorialId,
        UserInfo user,
        @Valid @RequestBody GuestbookRequest request) {
        return ResponseEntity.ok(guestbookService.create(memorialId, user, request));
    }

    @Operation(summary = "방명록 수정", description = "방명록을 수정합니다(작성자만 가능).")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Long entryId,
        UserInfo user,
        @Valid @RequestBody GuestbookRequest request) {
        guestbookService.update(entryId, user, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "방명록 삭제", description = "방명록을 삭제합니다(작성자만 가능).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long entryId,
        UserInfo user) {
        guestbookService.delete(entryId, user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "방명록 승인", description = "방명록을 승인합니다(관리자만 가능).")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable("id") Long entryId,
        UserInfo user) {
        guestbookService.approve(entryId, user);
        return ResponseEntity.ok().build();
    }
}
