package seg.work.geuliumieum.server.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.admin.dto.response.MemorialWithStatsResponse;
import seg.work.geuliumieum.server.admin.service.AdminMemorialService;

/**
 * 관리자용 추모관(Admin) API. - 특정 추모관의 상세 정보와 통계 데이터를 함께 조회합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/memorial")
public class AdminMemorialController {

    private final AdminMemorialService adminMemorialService;

    /**
     * 추모관 상세(통계 포함)를 조회합니다.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MemorialWithStatsResponse> getMemorialWithStats(@PathVariable("id") Long memorialId) {
        MemorialWithStatsResponse body = adminMemorialService.getMemorialWithStats(memorialId);
        return ResponseEntity.ok(body);
    }
}
