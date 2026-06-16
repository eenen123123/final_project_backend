package kr.or.ddit.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.or.ddit.service.impl.TuitionBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 원비 청구 배치 스케줄러
 * 매일 새벽 01:00 실행:
 *  - 결제일 3일 전 월 청구 선생성
 *  - 결제일 경과 미납 → 연체 전환
 *  - 누적 미납 3회 이상 → 블랙리스트 자동 등록
 *
 * ※ 수동 실행: GET /admin/billing/batch/run (관리자)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TuitionBillingScheduler {

    private final TuitionBatchService batchService;

    @Scheduled(cron = "0 0 1 * * *")
    public void runDaily() {
        log.info("[TuitionBillingScheduler] 원비 청구 배치 시작");
        batchService.runDailyBatch();
        log.info("[TuitionBillingScheduler] 원비 청구 배치 완료");
    }
}
