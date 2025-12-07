package seg.work.geuliumieum.server.tribute.controller;

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
import seg.work.geuliumieum.server.tribute.dto.request.TributeRequest;
import seg.work.geuliumieum.server.tribute.dto.response.TributeResponse;
import seg.work.geuliumieum.server.tribute.service.TributeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Tribute", description = "추모글 API")
public class TributeController {

    private final TributeService tributeService;

    @Operation(summary = "추모글 목록", description = "추모관별 추모글 목록을 조회합니다.")
    @GetMapping("/memorial/{id}/tribute/list")
    public ResponseEntity<Slice<TributeResponse>> list(@PathVariable("id") Long memorialId,
                                                       @ParameterObject Pageable pageable,
                                                       UserInfo user) {
        return ResponseEntity.ok(tributeService.listByMemorial(memorialId, pageable, user));
    }

    @Operation(summary = "추모글 작성", description = "해당 추모관에 추모글을 작성합니다.")
    @PostMapping("/memorial/{id}/tribute")
    public ResponseEntity<TributeResponse> create(@PathVariable("id") Long memorialId,
                                                  UserInfo user,
                                                  @Valid @RequestBody TributeRequest request) {
        return ResponseEntity.ok(tributeService.create(memorialId, user, request));
    }

    @Operation(summary = "추모글 수정", description = "추모글을 수정합니다(작성자만 가능).")
    @PutMapping("/tribute/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Long tributeId,
                                       UserInfo user,
                                       @Valid @RequestBody TributeRequest request) {
        tributeService.update(tributeId, user, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "추모글 삭제", description = "추모글을 삭제합니다(작성자만 가능).")
    @DeleteMapping("/tribute/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long tributeId,
                                       UserInfo user) {
        tributeService.delete(tributeId, user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자의 추모글 목록", description = "특정 사용자가 작성한 추모글 목록을 조회합니다.")
    @GetMapping("/user/{userId}/tribute/list")
    public ResponseEntity<Slice<TributeResponse>> listByUser(@PathVariable Long userId,
                                                             @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(tributeService.listByUser(userId, pageable));
    }
}
