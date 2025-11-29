package seg.work.geuliumieum.server.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DlqReprocessResponse {

    private final int requested;   // 요청한 최대 재처리 건수
    private final int fetched;     // DLQ에서 실제로 읽어온 건수
    private final int requeued;    // 메인 스트림으로 재투입한 건수
    private final int deleted;     // DLQ에서 삭제된 건수
    private final int failed;      // 처리 중 실패한 건수
}
