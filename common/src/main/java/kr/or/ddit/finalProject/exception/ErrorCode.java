package kr.or.ddit.finalProject.exception;

/**
 * 프로젝트 전반에서 발생할 수 있는 다양한 예외 상황을 정의하는 열거형 클래스
 * 각 예외 상황에 대한 고유한 코드와 메시지를 포함하여, 예외 처리 시 일관된 방식으로 사용할 수 있음
 */
public enum ErrorCode {
    // 요청 관련
    NOT_FOUND(404, "요청한 리소스를 찾을 수 없습니다."),
    BAD_REQUEST(400, "잘못된 요청입니다."),

    // 사용자 관련
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    USERNAME_ALREADY_EXISTS(409, "이미 사용중인 아이디입니다."),
    AUTHENTICATION_FAILED(401, "인증에 실패했습니다."),
    USERNAME_OR_PASSWORD_INCORRECT(401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_UNUSABLE(403, "사용할 수 없는 계정입니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),

    // 서버 관련
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다."),
    DOTENV_FILE_NOT_FOUND(500, "환경 변수 파일(.env)을 찾을 수 없습니다. .env.example 파일을 복사하여 .env 파일을 생성하고, 필요한 환경 변수를 설정해주세요.");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
