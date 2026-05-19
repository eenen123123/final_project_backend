package kr.or.ddit.finalProject.exception;

import org.springframework.http.HttpStatus;

/**
 * 프로젝트 전반에서 발생할 수 있는 다양한 예외 상황을 정의하는 열거형 클래스
 * 각 예외 상황에 대한 고유한 코드와 메시지를 포함하여, 예외 처리 시 일관된 방식으로 사용할 수 있음
 */
public enum ErrorCode {

    // 요청 관련
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용중인 아이디입니다."),

    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),

    USERNAME_OR_PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),

    ACCOUNT_UNUSABLE(HttpStatus.FORBIDDEN, "사용할 수 없는 계정입니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // 서버 관련
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    DOTENV_FILE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,
            "환경 변수 파일(.env)을 찾을 수 없습니다. .env.example 파일을 복사하여 .env 파일을 생성하고, 필요한 환경 변수를 설정해주세요."),


    // File
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),

    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),

    FILE_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다."),

    CANT_ACCESS_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "파일에 접근할 수 없습니다."),


    // 결제 관련

    // KAKAO

    KAKAO_PAY_READY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오페이 결제 준비에 실패했습니다."),

    KAKAO_PAY_APPROVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오페이 결제 승인에 실패했습니다.");


    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
