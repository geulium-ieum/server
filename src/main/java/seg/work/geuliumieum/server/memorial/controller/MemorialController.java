package seg.work.geuliumieum.server.memorial.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.common.dto.UserInfo;
import seg.work.geuliumieum.server.memorial.dto.request.RegisterRequest;
import seg.work.geuliumieum.server.memorial.dto.response.MemorialResponse;
import seg.work.geuliumieum.server.memorial.service.MemorialService;

/**
 * 추모관(Memorial) 공개 API.
 * - 단건 조회, 목록 조회(페이지네이션), 생성 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/memorial")
public class MemorialController {

    private final MemorialService memorialService;

    /** 추모관 단건을 조회합니다. */
    @GetMapping("/{id}")
    public MemorialResponse getMemorial(@PathVariable Long id) {
        return memorialService.getMemorial(id);
    }

    /** 추모관 목록을 페이지네이션 형태로 조회합니다. */
    @GetMapping("/list")
    public Slice<MemorialResponse> getMemorialList(Pageable pageable) {
        return memorialService.getMemorialList(pageable);
    }

    /** 현재 사용자 정보와 요청 본문을 받아 추모관을 생성합니다. */
    @PostMapping
    public void createMemorial(UserInfo userInfo, @Valid @RequestBody RegisterRequest request) {
        memorialService.createMemorial(userInfo, request);
    }
}
