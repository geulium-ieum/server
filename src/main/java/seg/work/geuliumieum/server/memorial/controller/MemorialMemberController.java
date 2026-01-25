package seg.work.geuliumieum.server.memorial.controller;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.memorial.dto.request.MemberAddRequest;
import seg.work.geuliumieum.server.memorial.dto.request.MemberRoleUpdateRequest;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialMemberResponse;
import seg.work.geuliumieum.server.memorial.service.MemorialMemberService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/memorial")
@Tag(name = "MemorialMember", description = "추모관 멤버 관리 API")
public class MemorialMemberController {

    private final MemorialMemberService memorialMemberService;

    @Operation(summary = "추모관 멤버 목록", description = "추모관 멤버 목록을 조회합니다(소유자만).")
    @GetMapping("/{id}/member/list")
    public ResponseEntity<Slice<MemorialMemberResponse>> members(UserInfo userInfo, @PathVariable("id") Long memorialId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(memorialMemberService.list(memorialId, pageable, userInfo));
    }

    @Operation(summary = "멤버 추가", description = "추모관에 멤버를 추가합니다(소유자만).")
    @PostMapping("/{id}/member")
    public ResponseEntity<Void> addMember(UserInfo userInfo, @PathVariable("id") Long memorialId, @Valid @RequestBody MemberAddRequest request) {
        memorialMemberService.add(memorialId, userInfo, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "멤버 역할 변경", description = "추모관 멤버의 역할을 변경합니다(소유자만).")
    @PutMapping("/{id}/member/{userId}")
    public ResponseEntity<Void> changeRole(UserInfo userInfo, @PathVariable("id") Long memorialId, @PathVariable Long userId, @Valid @RequestBody MemberRoleUpdateRequest request) {
        memorialMemberService.changeRole(memorialId, userId, userInfo, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "멤버 제거", description = "추모관에서 특정 멤버를 제거합니다(소유자만).")
    @DeleteMapping("/{id}/member/{userId}")
    public ResponseEntity<Void> removeMember(UserInfo userInfo, @PathVariable("id") Long memorialId, @PathVariable Long userId) {
        memorialMemberService.remove(memorialId, userId, userInfo);
        return ResponseEntity.ok().build();
    }
}
