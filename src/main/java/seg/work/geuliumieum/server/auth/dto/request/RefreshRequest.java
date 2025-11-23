package seg.work.geuliumieum.server.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequest {

    private String refreshToken;
}
