package seg.work.geuliumieum.server.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "유효성 검사 실패"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근이 거부되었습니다"),

    // Auth/JWT
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다"),
    JWT_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    JWT_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다"),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다"),

    // SNS
    GET_TOKEN_FROM_KAKAO_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 로그인에 실패하였습니다."),
    KAKAO_ACCESS_TOKEN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 로그인에 실패하였습니다."),
    KAKAO_GET_USER_INFO(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 로그인에 실패하였습니다."),
    GET_TOKEN_FROM_NAVER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 로그인에 실패하였습니다."),
    NAVER_ACCESS_TOKEN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 로그인에 실패하였습니다."),
    NAVER_GET_USER_INFO(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 로그인에 실패하였습니다."),

    // Email verification
    USER_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다"),
    VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "인증 코드가 올바르지 않습니다"),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다"),
    VERIFICATION_TOO_MANY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "인증 시도 횟수가 초과되었습니다"),
    VERIFICATION_RESEND_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS, "인증 코드를 다시 요청하기 전에 잠시 기다려주세요"),
    ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "이미 인증이 완료되었습니다"),

    // Password reset
    PASSWORD_RESET_CODE_INVALID(HttpStatus.BAD_REQUEST, "재설정 코드가 올바르지 않습니다"),
    PASSWORD_RESET_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "재설정 코드가 만료되었습니다"),
    PASSWORD_RESET_TOO_MANY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "재설정 시도 횟수가 초과되었습니다"),
    PASSWORD_RESET_RESEND_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS, "재설정 코드를 다시 요청하기 전에 잠시 기다려주세요"),
    PASSWORD_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "비밀번호 정책을 만족하지 않습니다"),

    // Family Group
    ALREADY_INVITATION(HttpStatus.BAD_REQUEST, "이미 초대된 사용자입니다. 초대장은 최대 5분동안 유효하므로 5분 후 재시도해주세요"),
    EXPIRED_INVITATION(HttpStatus.BAD_REQUEST, "만료된 초대입니다"),
    INVALID_INVITATION(HttpStatus.BAD_REQUEST, "유효하지 않은 초대입니다"),

    // User
    ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "이미 등록된 이메일입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    MEMORIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "추모관을 찾을 수 없습니다"),

    // Redis/Cache
    REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "캐시 처리 중 오류가 발생했습니다"),

    // File Upload
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 용량이 초과되었습니다"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다"),
    FILE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 중 오류가 발생했습니다"),

    // sort
    SORT_BAD_PROPERTY(HttpStatus.BAD_REQUEST, "정렬 대상이 잘못되었습니다")
    ;

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

}
