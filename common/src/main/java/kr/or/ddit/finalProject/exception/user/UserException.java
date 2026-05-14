package kr.or.ddit.finalProject.exception.user;

import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;

/**
 * 사용자 관련 예외를 처리하기 위한 커스텀 예외 클래스
 */
public class UserException extends FinalProjectException {

    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
