package seg.work.geuliumieum.server.offering.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.offering.dto.request.OfferingRequest;
import seg.work.geuliumieum.server.offering.dto.response.OfferingResponse;
import seg.work.geuliumieum.server.offering.dto.response.OfferingStatsResponse;
import seg.work.geuliumieum.server.offering.service.OfferingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/offering")
@Tag(name = "Offering", description = "헌화/분향/헌촛 API")
public class OfferingController {

    private final OfferingService offeringService;

    @Operation(summary = "헌화/분향/헌촛 목록", description = "추모관별 헌화/분향/헌촛 목록을 조회합니다.")
    @GetMapping("/memorial/{id}/list")
    public ResponseEntity<Slice<OfferingResponse>> list(UserInfo userInfo, @PathVariable("id") Long memorialId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(offeringService.listByMemorial(userInfo, memorialId, pageable));
    }

    @Operation(summary = "헌화/분향/헌촛 하기", description = "해당 추모관에 헌화/분향/헌촛을 등록합니다.")
    @PostMapping("/memorial/{id}")
    public ResponseEntity<OfferingResponse> create(UserInfo userInfo, @PathVariable("id") Long memorialId, @Valid @RequestBody OfferingRequest request) {
        return ResponseEntity.ok(offeringService.create(userInfo, memorialId, request));
    }

    @Operation(summary = "헌화/분향/헌촛 통계", description = "추모관별 헌화/분향/헌촛 통계를 조회합니다.")
    @GetMapping("/memorial/{id}/stats")
    public ResponseEntity<OfferingStatsResponse> stats(@PathVariable("id") Long memorialId) {
        return ResponseEntity.ok(offeringService.statsByMemorial(memorialId));
    }

    @Operation(summary = "사용자의 헌화/분향/헌촛 내역", description = "특정 사용자의 헌화/분향/헌촛 내역을 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Slice<OfferingResponse>> listByUser(@PathVariable Long userId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(offeringService.listByUser(userId, pageable));
    }
}
