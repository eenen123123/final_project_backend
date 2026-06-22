package kr.or.ddit.controller.pay;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import kr.or.ddit.finalProject.dto.order.CancelReason;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.pay.TossPayCancelService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CancelController {

    private final TossPayCancelService tossPayCancelService;

    // POST /api/orders/{ordSn}/cancel - 취소/환불 요청 (사용자)
    @PostMapping("/{ordSn}/cancel")
    public ResponseEntity<String> requestCancel(
            @PathVariable Long ordSn,
            @RequestBody CancelRequest request,
            Authentication authentication) {

        String userId = authentication.getName();

        if (request.getCancelRsnCd() == null) {
            throw new FinalProjectException(ErrorCode.CANCEL_REASON_REQUIRED);
        }
        if (request.getCancelRsnDtl() != null && request.getCancelRsnDtl().length() > 500) {
            throw new FinalProjectException(ErrorCode.CANCEL_REASON_TOO_LONG);
        }

        log.info("취소 요청 - ordSn: {}, userId: {}, reason: {}", ordSn, userId, request.getCancelRsnCd());
        tossPayCancelService.requestCancel(ordSn, userId, request.getCancelRsnCd(), request.getCancelRsnDtl());
        return ResponseEntity.ok("SUCCESS");
    }

    @Data
    static class CancelRequest {
        private CancelReason cancelRsnCd;
        private String cancelRsnDtl;
    }
}
