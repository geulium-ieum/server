package seg.work.geuliumieum.server.common.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seg.work.geuliumieum.server.common.exception.ErrorCode;
import seg.work.geuliumieum.server.common.exception.ErrorResponse;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        String errorMessage = ErrorCode.INTERNAL_ERROR.getDefaultMessage();

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            httpStatus = HttpStatus.resolve(statusCode);
            if (httpStatus == null) {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            if (httpStatus == HttpStatus.NOT_FOUND) {
                errorCode = ErrorCode.NOT_FOUND;
                errorMessage = "요청하신 경로를 찾을 수 없습니다.";
            } else if (httpStatus == HttpStatus.UNAUTHORIZED) {
                errorCode = ErrorCode.UNAUTHORIZED;
                errorMessage = ErrorCode.UNAUTHORIZED.getDefaultMessage();
            } else if (httpStatus == HttpStatus.FORBIDDEN) {
                errorCode = ErrorCode.FORBIDDEN;
                errorMessage = ErrorCode.FORBIDDEN.getDefaultMessage();
            }
        }

        // 구체적인 에러 메시지가 있으면 사용
        if (message != null && !message.toString().isEmpty()) {
            errorMessage = message.toString();
        }

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        if (exception != null) {
            details.put("exception", exception.getClass().getName());
        }

        ErrorResponse response = ErrorResponse.of(errorCode, errorMessage, details);
        return ResponseEntity.status(httpStatus).body(response);
    }
}
