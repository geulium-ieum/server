package seg.work.geuliumieum.server.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private OffsetDateTime timestamp;
    private int status;
    private String code;
    private String message;
    private Object details;

    public static ErrorResponse of(ErrorCode errorCode, String message, Object details) {
        return ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(errorCode.getStatus().value())
            .code(errorCode.name())
            .message(message != null ? message : errorCode.getDefaultMessage())
            .details(details)
            .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(errorCode.getStatus().value())
            .code(errorCode.name())
            .message(message != null ? message : errorCode.getDefaultMessage())
            .details(null)
            .build();
    }
}
