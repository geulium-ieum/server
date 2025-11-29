package seg.work.geuliumieum.server.auth.naver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NaverUserInfo {

    private String resultcode;
    private String message;
    private Response response;

    @Data
    public static class Response {

        private String id;
        private String email;
        private String name;
        private String nickname;
        @JsonProperty("profile_image")
        private String profileImage;
    }
}
