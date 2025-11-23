package seg.work.geuliumieum.server.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.admin.dto.response.MemorialWithStatsResponse;
import seg.work.geuliumieum.server.admin.service.AdminMemorialService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/memorials")
public class AdminMemorialController {

    private final AdminMemorialService adminMemorialService;

    @GetMapping("/{id}")
    public ResponseEntity<MemorialWithStatsResponse> getMemorialWithStats(@PathVariable("id") Long memorialId) {
        MemorialWithStatsResponse body = adminMemorialService.getMemorialWithStats(memorialId);
        return ResponseEntity.ok(body);
    }
}
