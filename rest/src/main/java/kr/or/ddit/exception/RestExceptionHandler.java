package kr.or.ddit.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * REST API 전반에서 발생할 수 있는 예외를 처리하기 위한 글로벌 예외 핸들러 클래스
 */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(FinalProjectException.class)
    public ResponseEntity<ErrorResponse> handle(FinalProjectException ex) {
        ErrorCode code = ex.getErrorCode();
        log.error("Exception caught: {}", code.getMessage());

        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(code.getStatus(), code.getMessage()));
    }

    record ErrorResponse(int status, String message) {
    }
}
