package kr.or.ddit.finalProject.exception;

/**
 * 프로젝트 전반에서 발생할 수 있는 예외를 처리하기 위한 커스텀 예외 클래스
 * 각 모듈에서 발생하는 특정 예외는 이 클래스를 상속하여 구현할 수 있음
 */
public class FinalProjectException extends RuntimeException {
    private final ErrorCode errorCode;

    public FinalProjectException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /** 상태코드는 ErrorCode를 따르되, 응답 메시지를 동적으로 지정할 때 사용 */
    public FinalProjectException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FinalProjectException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }


    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
