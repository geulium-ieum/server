package seg.work.geuliumieum.server.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.user.dto.response.UserMeResponse;
import seg.work.geuliumieum.server.user.service.UserService;

/**
 * 사용자(User) API.
 * - 현재 로그인한 사용자의 기본 정보를 조회합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    /** 현재 로그인한 사용자의 정보를 반환합니다. */
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(UserInfo user) {
        UserMeResponse me = userService.getCurrentUser(user == null ? null : user.getId());
        return ResponseEntity.ok(me);
    }
}
