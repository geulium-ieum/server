package seg.work.geuliumieum.server.memorial.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.memorial.dto.request.RegisterRequest;
import seg.work.geuliumieum.server.memorial.service.MemorialService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/memorial")
public class MemorialController {

    private final MemorialService memorialService;

    @GetMapping("/list")
    public void list() {
    }

    @GetMapping("/{id}")
    public void getMemorial(@PathVariable String id) {
    }

    @PostMapping
    public void createMemorial(UserInfo userInfo, @Valid @RequestBody RegisterRequest request) {
        memorialService.createMemorial(userInfo, request);
    }
}
