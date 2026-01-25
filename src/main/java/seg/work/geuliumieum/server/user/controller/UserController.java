package seg.work.geuliumieum.server.user.controller;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;
import seg.work.geuliumieum.server.user.dto.request.ProfilePhotoUpdateRequest;
import seg.work.geuliumieum.server.user.dto.request.UserUpdateRequest;
import seg.work.geuliumieum.server.user.dto.response.UserActivityResponse;
import seg.work.geuliumieum.server.user.dto.response.UserMeResponse;
import seg.work.geuliumieum.server.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "User", description = "사용자 정보 조회 API")
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인한 사용자의 정보를 반환합니다.
     */
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 기본 프로필 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(UserInfo userInfo) {
        UserMeResponse me = userService.getCurrentUser(userInfo == null ? null : userInfo.getId());
        return ResponseEntity.ok(me);
    }

    @Operation(summary = "사용자 프로필 조회", description = "ID로 사용자 프로필을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<UserMeResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자 본인만 수정 가능합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(UserInfo userInfo, @PathVariable Long id, @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, userInfo == null ? null : userInfo.getId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 삭제", description = "사용자 본인만 삭제 가능합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(UserInfo userInfo, @PathVariable Long id) {
        userService.deleteUser(id, userInfo == null ? null : userInfo.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "프로필 사진 업데이트", description = "사전에 업로드된 S3 URL을 등록합니다.")
    @PatchMapping("/{id}/profile-photo")
    public ResponseEntity<Void> updateProfilePhoto(UserInfo userInfo, @PathVariable Long id, @RequestBody ProfilePhotoUpdateRequest request) {
        userService.updateProfilePhoto(id, userInfo == null ? null : userInfo.getId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 활동 통계", description = "추모글/헌화/방명록 수를 조회합니다.")
    @GetMapping("/{id}/activity")
    public ResponseEntity<UserActivityResponse> getUserActivity(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserActivity(id));
    }

    @Operation(summary = "내가 생성한 추모관 목록", description = "사용자가 생성한 추모관 목록을 조회합니다.")
    @GetMapping("/{id}/memorial/list")
    public ResponseEntity<Slice<MemorialResponse>> getMyMemorials(@PathVariable Long id, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.getCreatedMemorials(id, pageable));
    }

    @Operation(summary = "내가 참여한 추모관 목록", description = "사용자가 멤버로 참여한 추모관 목록을 조회합니다.")
    @GetMapping("/{id}/joined-memorial/list")
    public ResponseEntity<Slice<MemorialResponse>> getJoinedMemorials(@PathVariable Long id, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.getJoinedMemorials(id, pageable));
    }
}
