package seg.work.geuliumieum.server.common.exception.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import seg.work.geuliumieum.server.config.i18n.MessageUtil;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.exception.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageUtil messageUtil;

    public GlobalExceptionHandler(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        log.error("ApiException occurred: {}", ex.getMessage(), ex);

        String localized = messageUtil.get("error." + ex.getErrorCode().name());
        String message = (ex.getMessage() != null && !ex.getMessage().isBlank()) ? ex.getMessage() : localized;
        ErrorResponse body = ErrorResponse.of(ex.getErrorCode(), message, ex.getDetails());
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException occurred: {}", ex.getMessage(), ex);

        List<Map<String, Object>> details = ex.getBindingResult().getFieldErrors().stream()
            .map(this::toFieldError)
            .collect(Collectors.toList());
        String message = messageUtil.get("error." + ErrorCode.VALIDATION_FAILED.name());
        ErrorResponse body = ErrorResponse.of(ErrorCode.VALIDATION_FAILED, message, details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.error("BadCredentialsException occurred: {}", ex.getMessage(), ex);

        String message = messageUtil.get("error." + ErrorCode.AUTH_INVALID_CREDENTIALS.name());
        ErrorResponse body = ErrorResponse.of(ErrorCode.AUTH_INVALID_CREDENTIALS, message, null);
        return ResponseEntity.status(ErrorCode.AUTH_INVALID_CREDENTIALS.getStatus()).body(body);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.error("UsernameNotFoundException occurred: {}", ex.getMessage(), ex);

        String message = messageUtil.get("error." + ErrorCode.USER_NOT_FOUND.name());
        ErrorResponse body = ErrorResponse.of(ErrorCode.USER_NOT_FOUND, message, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        log.error("ResponseStatusException occurred: {}", ex.getMessage(), ex);

        HttpStatus status = ex.getStatusCode() instanceof HttpStatus ? (HttpStatus) ex.getStatusCode() : HttpStatus.BAD_REQUEST;
        String message = ex.getReason() != null ? ex.getReason() : messageUtil.get("error." + ErrorCode.BAD_REQUEST.name());
        ErrorResponse body = ErrorResponse.of(ErrorCode.BAD_REQUEST, message, null);
        return ResponseEntity.status(status).headers(new HttpHeaders()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex) {
        log.error("Exception occurred: {}", ex.getMessage(), ex);

        String message = messageUtil.get("error." + ErrorCode.INTERNAL_ERROR.name());
        ErrorResponse body = ErrorResponse.of(ErrorCode.INTERNAL_ERROR, message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private Map<String, Object> toFieldError(FieldError fe) {
        Map<String, Object> m = new HashMap<>();
        m.put("field", fe.getField());
        m.put("rejectedValue", fe.getRejectedValue());
        m.put("message", fe.getDefaultMessage());
        return m;
    }
}
