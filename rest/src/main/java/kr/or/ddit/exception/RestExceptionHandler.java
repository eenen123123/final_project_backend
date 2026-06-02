package kr.or.ddit.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    // 커스텀 예외
    @ExceptionHandler(FinalProjectException.class)
    public ResponseEntity<ErrorResponse> handle(FinalProjectException ex) {
        ErrorCode code = ex.getErrorCode();
        log.error("[FinalProjectException] {}", code.getMessage(), ex);
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(code.getStatus().value(), code.getMessage()));
    }

    // @Valid / @Validated 검증 실패 — 필드별 에러 메시지를 하나로 합쳐서 반환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("[Validation] {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message));
    }

    // 요청 바디 JSON 파싱 실패 (형식 오류, 필드 타입 불일치 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handle(HttpMessageNotReadableException ex) {
        log.warn("[MessageNotReadable] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "요청 형식이 올바르지 않습니다."));
    }

    // 그 외 예상치 못한 예외 — 500으로 내리고 스택 트레이스 로깅
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception ex) {
        log.error("[Unhandled Exception]", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
    }

    record ErrorResponse(int status, String message) {}
}
