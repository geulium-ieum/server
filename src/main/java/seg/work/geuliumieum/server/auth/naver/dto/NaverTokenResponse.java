package seg.work.geuliumieum.server.auth.naver.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverTokenResponse {

    private String access_token;
    private String refresh_token;
    private String token_type;
    private Integer expires_in;
    private String error;
    private String error_description;

    public String getAccessToken() {
        return access_token;
    }
}
