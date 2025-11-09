package seg.work.geuliumieum.server.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindIdResponse {

    private String maskedEmail;
}
