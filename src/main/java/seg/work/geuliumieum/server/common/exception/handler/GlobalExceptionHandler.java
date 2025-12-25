package seg.work.geuliumieum.server.common.exception.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import seg.work.geuliumieum.server.common.exception.ApiException;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.exception.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        log.error("ApiException occurred: {}", ex.getMessage(), ex);

        ErrorResponse body = ErrorResponse.of(
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getDetails()
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException occurred: {}", ex.getMessage(), ex);

        List<Map<String, Object>> details = ex.getBindingResult().getFieldErrors().stream()
            .map(this::toFieldError)
            .collect(Collectors.toList());
        ErrorResponse body = ErrorResponse.of(
            ErrorCode.VALIDATION_FAILED,
            ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.error("BadCredentialsException occurred: {}", ex.getMessage(), ex);

        ErrorResponse body = ErrorResponse.of(
            ErrorCode.AUTH_INVALID_CREDENTIALS,
            ErrorCode.AUTH_INVALID_CREDENTIALS.getDefaultMessage(),
            null
        );
        return ResponseEntity.status(ErrorCode.AUTH_INVALID_CREDENTIALS.getStatus()).body(body);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.error("UsernameNotFoundException occurred: {}", ex.getMessage(), ex);

        ErrorResponse body = ErrorResponse.of(
            ErrorCode.USER_NOT_FOUND,
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        log.error("ResponseStatusException occurred: {}", ex.getMessage(), ex);

        HttpStatus status = ex.getStatusCode() instanceof HttpStatus ? (HttpStatus) ex.getStatusCode() : HttpStatus.BAD_REQUEST;
        ErrorResponse body = ErrorResponse.of(
            ErrorCode.BAD_REQUEST,
            ex.getReason(),
            null
        );
        return ResponseEntity.status(status).headers(new HttpHeaders()).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.error("AccessDeniedException occurred: {}", ex.getMessage(), ex);

        ErrorResponse body = ErrorResponse.of(
            ErrorCode.FORBIDDEN,
            ErrorCode.FORBIDDEN.getDefaultMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.error("HttpRequestMethodNotSupportedException occurred: {}", ex.getMessage(), ex);

        ErrorResponse body = ErrorResponse.of(
            ErrorCode.BAD_REQUEST,
            "지원하지 않는 HTTP 메서드입니다: " + ex.getMethod(),
            null
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("NoResourceFoundException occurred: {}", ex.getMessage());

        ErrorResponse body = ErrorResponse.of(
            ErrorCode.NOT_FOUND,
            "요청하신 리소스를 찾을 수 없습니다: " + ex.getResourcePath(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex) {
        log.error("Exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse body = ErrorResponse.of(
            ErrorCode.INTERNAL_ERROR,
            ErrorCode.INTERNAL_ERROR.getDefaultMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private Map<String, Object> toFieldError(FieldError fe) {
        Map<String, Object> map = new HashMap<>();
        map.put("field", fe.getField());
        map.put("rejectedValue", fe.getRejectedValue());
        map.put("message", fe.getDefaultMessage());
        return map;
    }
}
