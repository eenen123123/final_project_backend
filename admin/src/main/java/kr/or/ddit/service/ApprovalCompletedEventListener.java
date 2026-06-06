package kr.or.ddit.service;

import kr.or.ddit.service.event.ApprovalCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalCompletedEventListener {

    private final AdminActivityExecutionService executionService;

    // AFTER_COMMIT: 결재 트랜잭션이 커밋된 후에 실행 → 실패해도 결재 상태는 유지됨
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApprovalCompleted(ApprovalCompletedEvent event) {
        log.info("[ApprovalListener] 최종 결재 승인 감지: docSn={}", event.getAprvlDocSn());
        executionService.execute(event.getAprvlDocSn());
    }
}
