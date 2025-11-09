package seg.work.geuliumieum.server.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.user.dto.UserInfo;
import seg.work.geuliumieum.server.user.dto.UserMeResponse;
import seg.work.geuliumieum.server.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(UserInfo user) {
        UserMeResponse me = userService.getCurrentUser(user == null ? null : user.getId());
        return ResponseEntity.ok(me);
    }
}
