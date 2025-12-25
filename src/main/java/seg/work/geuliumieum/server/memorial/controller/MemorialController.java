package seg.work.geuliumieum.server.memorial.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.memorial.dto.request.RegisterRequest;
import seg.work.geuliumieum.server.memorial.dto.request.UpdateRequest;
import seg.work.geuliumieum.server.memorial.dto.response.AccessResponse;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;
import seg.work.geuliumieum.server.memorial.service.MemorialService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/memorial")
@Tag(name = "Memorial", description = "추모관 조회/목록/생성 공개 API")
public class MemorialController {

    private final MemorialService memorialService;

    /**
     * 추모관 단건을 조회합니다.
     */
    @Operation(summary = "추모관 단건 조회", description = "ID로 특정 추모관 상세를 조회합니다.")
    @GetMapping("/{id}")
    public MemorialResponse getMemorial(UserInfo user, @Parameter(description = "추모관 ID", example = "1001") @PathVariable Long id) {
        return memorialService.getMemorial(id, user);
    }

    /**
     * 추모관 목록을 페이지네이션 형태로 조회합니다.
     */
    @Operation(summary = "추모관 목록 조회", description = "페이지네이션으로 추모관 목록을 조회합니다.")
    @GetMapping("/list")
    public Slice<MemorialResponse> getMemorialList(@ParameterObject Pageable pageable) {
        return memorialService.getMemorialList(pageable);
    }

    /**
     * 추모관을 수정합니다.
     */
    @Operation(summary = "추모관 수정", description = "ID로 특정 추모관의 정보를 수정합니다.")
    @PutMapping("/{id}")
    public void updateMemorial(UserInfo userInfo, @Parameter(description = "추모관 ID", example = "1001") @PathVariable Long id, @Valid @RequestBody UpdateRequest request) {
        memorialService.updateMemorial(userInfo, id, request);
    }

    /**
     * 추모관을 삭제합니다.
     */
    @Operation(summary = "추모관 삭제", description = "ID로 특정 추모관을 삭제합니다.")
    @DeleteMapping("/{id}")
    public void deleteMemorial(UserInfo userInfo, @Parameter(description = "추모관 ID", example = "1001") @PathVariable Long id) {
        memorialService.deleteMemorial(userInfo, id);
    }

    /**
     * 날짜로 필터링합니다.
     */
    @Operation(summary = "추모관 필터", description = "생/사망일로 필터링합니다.")
    @GetMapping("/filter")
    public Slice<MemorialResponse> filterMemorials(
        @Parameter(description = "고인 이름(부분 일치)") String name,
        @Parameter(description = "출생일 시작") String birthFrom,
        @Parameter(description = "출생일 종료") String birthTo,
        @Parameter(description = "사망일 시작") String deathFrom,
        @Parameter(description = "사망일 종료") String deathTo,
        @ParameterObject Pageable pageable) {
        return memorialService.filter(name, birthFrom, birthTo, deathFrom, deathTo, pageable);
    }

    /**
     * 현재 사용자의 추모관 접근 권한을 확인합니다.
     */
    @Operation(summary = "추모관 접근 권한 확인", description = "현재 사용자의 접근 권한을 확인합니다.")
    @GetMapping("/{id}/access")
    public AccessResponse checkAccess(UserInfo userInfo, @Parameter(description = "추모관 ID", example = "1001") @PathVariable Long id) {
        return memorialService.getAccess(userInfo, id);
    }

    /**
     * 현재 사용자 정보와 요청 본문을 받아 추모관을 생성합니다.
     */
    @Operation(summary = "추모관 생성", description = "현재 사용자 컨텍스트로 새로운 추모관을 생성합니다.")
    @PostMapping
    public void createMemorial(UserInfo userInfo, @Valid @RequestBody RegisterRequest request) {
        memorialService.createMemorial(userInfo, request);
    }
}
