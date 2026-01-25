package seg.work.geuliumieum.server.familygroup.controller;

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
import seg.work.geuliumieum.server.familygroup.dto.request.AddMemorialRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.FamilyGroupCreateRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.FamilyGroupUpdateRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.InviteRequest;
import seg.work.geuliumieum.server.familygroup.dto.request.MemberRoleUpdateRequest;
import seg.work.geuliumieum.server.familygroup.dto.response.FamilyGroupMemberResponse;
import seg.work.geuliumieum.server.familygroup.dto.response.FamilyGroupResponse;
import seg.work.geuliumieum.server.familygroup.service.FamilyGroupService;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/family-group")
@Tag(name = "FamilyGroup", description = "가족 그룹 API")
public class FamilyGroupController {

    private final FamilyGroupService familyGroupService;

    // 4.1 가족 그룹 기본 CRUD
    @Operation(summary = "내 가족 그룹 목록", description = "현재 사용자가 소유/참여 중인 가족 그룹 목록")
    @GetMapping("/list")
    public ResponseEntity<Slice<FamilyGroupResponse>> myGroups(UserInfo userInfo, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(familyGroupService.myGroups(userInfo, pageable));
    }

    @Operation(summary = "가족 그룹 상세", description = "가족 그룹 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<FamilyGroupResponse> get(UserInfo userInfo, @PathVariable Long id) {
        return ResponseEntity.ok(familyGroupService.get(userInfo, id));
    }

    @Operation(summary = "가족 그룹 생성", description = "가족 그룹을 생성합니다")
    @PostMapping
    public ResponseEntity<Void> create(UserInfo userInfo, @Valid @RequestBody FamilyGroupCreateRequest request) {
        familyGroupService.create(userInfo, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "가족 그룹 수정", description = "가족 그룹을 수정합니다(소유자만)")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(UserInfo userInfo, @PathVariable Long id, @Valid @RequestBody FamilyGroupUpdateRequest request) {
        familyGroupService.update(userInfo, id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "가족 그룹 삭제", description = "가족 그룹을 삭제합니다(소유자만)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(UserInfo userInfo, @PathVariable Long id) {
        familyGroupService.delete(userInfo, id);
        return ResponseEntity.ok().build();
    }

    // 4.2 가족 그룹 멤버 관리
    @Operation(summary = "그룹 멤버 목록", description = "가족 그룹 멤버 목록을 조회합니다")
    @GetMapping("/{id}/member/list")
    public ResponseEntity<Slice<FamilyGroupMemberResponse>> members(UserInfo userInfo, @PathVariable("id") Long groupId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(familyGroupService.members(userInfo, groupId, pageable));
    }

    @Operation(summary = "멤버 초대", description = "그룹에 멤버를 초대/추가합니다(소유자만)")
    @PostMapping("/{id}/invite")
    public ResponseEntity<Void> invite(UserInfo userInfo, @PathVariable("id") Long groupId, @Valid @RequestBody InviteRequest request) {
        familyGroupService.invite(userInfo, groupId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "초대 수락/가입", description = "그룹에 가입합니다(본인)")
    @PostMapping("/{id}/join")
    public ResponseEntity<Void> join(UserInfo userInfo, @PathVariable("id") Long groupId) {
        familyGroupService.join(userInfo, groupId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "멤버 제거", description = "그룹에서 특정 멤버를 제거합니다(소유자만)")
    @DeleteMapping("/{id}/member/{userId}")
    public ResponseEntity<Void> remove(UserInfo userInfo, @PathVariable("id") Long groupId, @PathVariable Long userId) {
        familyGroupService.removeMember(userInfo, groupId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "멤버 역할 변경", description = "그룹 멤버의 역할을 변경합니다(소유자만)")
    @PatchMapping("/{id}/member/{userId}/role")
    public ResponseEntity<Void> changeRole(UserInfo userInfo, @PathVariable("id") Long groupId, @PathVariable Long userId, @Valid @RequestBody MemberRoleUpdateRequest request) {
        familyGroupService.changeRole(userInfo, groupId, userId, request);
        return ResponseEntity.ok().build();
    }

    // 4.3 가족 그룹 추모관 관리
    @Operation(summary = "그룹 추모관 목록", description = "가족 그룹에 연결된 추모관 목록")
    @GetMapping("/{id}/memorial/list")
    public ResponseEntity<Slice<MemorialResponse>> groupMemorials(UserInfo userInfo, @PathVariable("id") Long groupId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(familyGroupService.groupMemorials(userInfo, groupId, pageable));
    }

    @Operation(summary = "그룹에 추모관 추가", description = "가족 그룹에 추모관을 연결합니다(소유자만)")
    @PostMapping("/{id}/memorial")
    public ResponseEntity<Void> addMemorial(UserInfo userInfo, @PathVariable("id") Long groupId, @Valid @RequestBody AddMemorialRequest request) {
        familyGroupService.addMemorial(userInfo, groupId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "그룹에서 추모관 제거", description = "가족 그룹에서 추모관 연결을 제거합니다(소유자만)")
    @DeleteMapping("/{id}/memorial/{memorialId}")
    public ResponseEntity<Void> removeMemorial(UserInfo userInfo, @PathVariable("id") Long groupId, @PathVariable Long memorialId) {
        familyGroupService.removeMemorial(userInfo, groupId, memorialId);
        return ResponseEntity.ok().build();
    }
}
