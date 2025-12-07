package seg.work.geuliumieum.server.stats.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.stats.dto.MemorialStatsResponse;
import seg.work.geuliumieum.server.stats.dto.StatsOverviewResponse;
import seg.work.geuliumieum.server.stats.service.StatsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
@Tag(name = "Stats", description = "공개 통계 API")
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "시스템 전체 통계", description = "시스템 전반의 누적 통계를 조회합니다.")
    @GetMapping("/overview")
    public ResponseEntity<StatsOverviewResponse> overview() {
        return ResponseEntity.ok(statsService.overview());
    }

    @Operation(summary = "추모관 통계", description = "특정 추모관의 요약 통계를 조회합니다.")
    @GetMapping("/memorial/{id}")
    public ResponseEntity<MemorialStatsResponse> memorial(@PathVariable("id") Long memorialId) {
        return ResponseEntity.ok(statsService.memorialStats(memorialId));
    }
}
