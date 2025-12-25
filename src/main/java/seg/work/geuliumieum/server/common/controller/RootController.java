package seg.work.geuliumieum.server.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Root", description = "기본 연결 확인 API")
public class RootController {

    @Operation(summary = "헬스 체크", description = "서버 연결 상태를 확인합니다.")
    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of("status", "UP", "service", "geulium-ieum API Server");
    }
}
