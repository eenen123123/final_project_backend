package kr.or.ddit.exception;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import kr.or.ddit.finalProject.dto.log.SystemErrorLogDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.log.SystemErrorLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class AdminExceptionHandler {

    private final SystemErrorLogMapper errorLogMapper;

    // 커스텀 예외
    @ExceptionHandler(FinalProjectException.class)
    public Object handle(FinalProjectException ex, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        ErrorCode code = ex.getErrorCode();
        log.error("[FinalProjectException] {}", code.getMessage(), ex);

        saveErrorLog(code.name(), request.getRequestURI(), code.getMessage());

        if (isAjax(request)) {
            return ResponseEntity.status(code.getStatus())
                    .body(new ErrorResponse(code.getStatus().value(), code.getMessage()));
        }
        redirectAttributes.addFlashAttribute("status", code.getStatus().value());
        redirectAttributes.addFlashAttribute("errorMessage", code.getMessage());
        return "redirect:/admin/error";
    }

    // @Valid / @Validated 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handle(MethodArgumentNotValidException ex, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("[Validation] {}", message);
        
        saveErrorLog("VALIDATION_FAILED", request.getRequestURI(), message);

        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message));
        }
        redirectAttributes.addFlashAttribute("status", HttpStatus.BAD_REQUEST.value());
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:/admin/error";
    }

    // 요청 바디 JSON 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handle(HttpMessageNotReadableException ex, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        log.warn("[MessageNotReadable] {}", ex.getMessage());

        saveErrorLog("INVALID_REQUEST_BODY", request.getRequestURI(), ex.getMessage());

        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "요청 형식이 올바르지 않습니다."));
        }
        redirectAttributes.addFlashAttribute("status", HttpStatus.BAD_REQUEST.value());
        redirectAttributes.addFlashAttribute("errorMessage", "요청 형식이 올바르지 않습니다.");
        return "redirect:/admin/error";
    }

    // 정적 리소스 404 (브라우저 확장 프로그램 등이 .map 파일 요청하는 경우 포함)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handle(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("[NoResource] {}", ex.getMessage());

        saveErrorLog("NOT_FOUND", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.notFound().build();
    }

    // 그 외 예상치 못한 예외
    @ExceptionHandler(Exception.class)
    public Object handle(Exception ex, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        log.error("[Unhandled Exception]", ex);

        saveErrorLog(ex.getClass().getSimpleName(), request.getRequestURI(), ex.getMessage());

        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
        }
        redirectAttributes.addFlashAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        redirectAttributes.addFlashAttribute("errorMessage", "서버 내부 오류가 발생했습니다.");
        return "redirect:/admin/error";
    }

    private void saveErrorLog(String errorCode, String requestUri, String errorMessage) {
        try {
            errorLogMapper.insertSystemErrorLog(
                SystemErrorLogDto.builder()
                    .traceId(UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                    .errorCode(truncate(errorCode, 50))
                    .requestUri(truncate(requestUri, 255))
                    .errorMessage(truncate(errorMessage, 2000))
                    .build()
            );
        } catch (Exception e) {
            log.warn("[AdminExceptionHandler] 에러 로그 저장 실패: {}", e.getMessage());
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    record ErrorResponse(int status, String message) {}
}
